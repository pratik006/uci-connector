package com.prapps.client.udp.cb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.UCIUtil;

public class ChessbaseAdapter {

	private static Logger LOG = Logger.getLogger(ChessbaseAdapter.class.getName());
	public static final int MAX_CONN_ATTEMPT = 5;
	public static final String START_MSG = "helloserver";
	public static final String CONN_SUCCESS_MSG = "connected";
	public static final String QUIT_MSG = "quit";
	public static final String CLOSE_MSG = "close";

	private DatagramSocket datagramSocket = null;
	private Properties config;
	private boolean exit = false;
	private int adminPort;
	private int targetPort;
	private Thread cbReader;
	private Thread cbWriter;
	private InputStream consoleInputStream = System.in;
	private InetAddress targetAddress;
	private byte[] password;

	private void listen() throws FileNotFoundException, IOException, InterruptedException {
		
		config = new Properties();
		config.load(new FileInputStream("clientConfig.ini"));
		adminPort = Integer.parseInt(config.getProperty("admin_port"));
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			public void run() {
				try {
					close();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}));
		/*if(null != config.getProperty("auto_config") && "true".equalsIgnoreCase(config.getProperty("auto_config"))) {
			try {
				String params[] = UCIUtil.getIPFromMail(config.getProperty("clientMail"), config.getProperty("clientMailPass")).split(":");
				targetAddress = InetAddress.getByName(UCIUtil.getParam(params, 0));				
				targetPort = Integer.parseInt(UCIUtil.getParam(params, 1))+Integer.parseInt(config.getProperty("target_port"));
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			targetAddress = InetAddress.getByName(config.getProperty("target_ip"));
		}*/
		targetAddress = InetAddress.getByName(config.getProperty("target_ip"));
		targetPort = adminPort + Integer.parseInt(config.getProperty("target_port"));
		
		LOG.info("server: "+targetAddress+"port: "+targetPort);	
		//password = config.getProperty("secretKey").getBytes();
		password = "test123".getBytes();
		datagramSocket = new DatagramSocket();

		if (!connect()) {
			System.out.print("Cannot connect to server " + targetAddress + ":" + targetPort);
		}

		cbWriter = new Thread(new Runnable() {

			public void run() {
				byte[] buf = new byte[ProtocolConstants.BUFFER_SIZE];
				OutputStream os = System.out;
				DatagramPacket inputPacket;
				while (!exit) {
					inputPacket = new DatagramPacket(buf, buf.length);
					try {
						datagramSocket.receive(inputPacket);
						os.write(UCIUtil.readBytePacket(inputPacket, password));
						os.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				LOG.info("Closing CB Writer");
				try {
					consoleInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		cbReader = new Thread(new Runnable() {

			public void run() {
				byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];
				try {
					int readLen = buffer.length;
					while (!exit && (readLen = consoleInputStream.read(buffer, 0, buffer.length)) != -1) {
						UCIUtil.sendPacket(Arrays.copyOfRange(buffer, 0, readLen), password, datagramSocket, targetAddress, targetPort);
						if (new String(buffer, 0, readLen).contains("quit"))
							exit = true;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				LOG.info("closing cbReader");
			}

		});

		cbReader.start();
		cbWriter.start();

		if (cbReader.isAlive())
			cbReader.join();
		if (cbWriter.isAlive())
			cbWriter.join();

		LOG.finest("Closing CB Adapter");
	}

	public String udpToString(DatagramPacket p) {
		return new String(p.getData(), 0, p.getLength());
	}

	public boolean connect() throws IOException {
		DatagramPacket connPacket = new DatagramPacket(new byte[ProtocolConstants.BUFFER_SIZE], ProtocolConstants.BUFFER_SIZE);
		String connResp = null;
		int connAttepmt = 0;
		do {
			connAttepmt++;
			UCIUtil.sendPacket(START_MSG.getBytes(UCIUtil.CHARSET), password, datagramSocket, targetAddress, targetPort);
			LOG.fine("sent connection initator packet :" + START_MSG + " to " + targetAddress + ":" + targetPort);
			datagramSocket.receive(connPacket);
			connResp = UCIUtil.readPacket(connPacket, password);
			LOG.fine("Server :" + connResp);
			if (connResp.equalsIgnoreCase(CONN_SUCCESS_MSG))
				return true;
		} while (connAttepmt < MAX_CONN_ATTEMPT);

		return false;
	}
	
	public void close() throws UnsupportedEncodingException {
		LOG.fine("closing client packet :" + QUIT_MSG + " to " + targetAddress + ":" + targetPort);
		UCIUtil.sendPacket(QUIT_MSG.getBytes(UCIUtil.CHARSET), password, datagramSocket, targetAddress, targetPort);
		LOG.fine("closing client packet :" + CLOSE_MSG + " to " + targetAddress + ":" + targetPort);
		UCIUtil.sendPacket(CLOSE_MSG.getBytes(UCIUtil.CHARSET), password, datagramSocket, targetAddress, targetPort);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		System.getProperties().put("Djava.util.logging.config.file", "D:\\JavaWorks\\UCIHost\\UCIHost\\logging.properties");
		ChessbaseAdapter adapter = new ChessbaseAdapter();
		adapter.listen();
	}

}
