package com.prapps.chess.client.tcp.cb;

import static com.prapps.chess.uci.share.ProtocolConstants.CLOSE_MSG;
import static com.prapps.chess.uci.share.ProtocolConstants.CONN_SUCCESS_MSG;
import static com.prapps.chess.uci.share.ProtocolConstants.QUIT_MSG;
import static com.prapps.chess.uci.share.ProtocolConstants.SET_PROTOCOL_TCP;
import static com.prapps.chess.uci.share.ProtocolConstants.START_MSG;
import static com.prapps.chess.uci.share.ProtocolConstants.SUCCESS_SET_PROTOCOL_TCP;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.NetworkRW;
import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.TCPNetworkRW;
import com.prapps.chess.uci.share.UCIUtil;

public class ChessbaseClientAdapter {

	private static Logger LOG = Logger.getLogger(ChessbaseClientAdapter.class.getName());
	public static final int MAX_CONN_ATTEMPT = 5;
	

	private NetworkRW networkRW;
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
		LOG.finest("Property config file loaded");
		String networkPref = config.getProperty("network_pref");
		if("local".equals(networkPref)) {
			LOG.info("trying local ip");
			try {
				tryLocalIP();
			}
			catch(IOException ex) {
				ex.printStackTrace();
				tryManual();
			}
		}
		else if("external".equals(networkPref)) {
			LOG.info("trying external ip");
			try {
				tryExternalServerApproach();
			}
			catch(IOException ex) {
				ex.printStackTrace();
				tryManual();
			}
		}
		else {
			LOG.info("trying manual ip");
			tryManual();
		}		
		
		password = "test123".getBytes();
		cbWriter = new Thread(new Runnable() {

			public void run() {
				String serverMsg = null;
				while (!exit) {
					try {
						serverMsg = networkRW.readFromNetwork();
						LOG.finest("Server: "+serverMsg);
						if("exit".equalsIgnoreCase(serverMsg)) {
							networkRW.close();
							exit = true;
						}
						else {
							System.out.write(serverMsg.getBytes());
							System.out.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				LOG.finest("Closing CB Writer");
				try {
					System.out.write("quit".getBytes());
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
						LOG.finest("Chessbase: "+new String(buffer, 0, readLen));
						networkRW.writeToNetwork(Arrays.copyOfRange(buffer, 0, readLen));
						if (new String(buffer, 0, readLen).contains("quit")) {
							exit = true;
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				LOG.finest("closing cbReader");
			}

		});

		cbReader.start();
		cbWriter.start();

		if (cbWriter.isAlive())
			cbWriter.join();
		/*if (cbReader.isAlive())
			cbReader.join();*/
		cbReader.interrupt();
		cbReader.stop();
		LOG.finest("Closing CB Adapter");
		System.exit(0);
	}

	public String udpToString(DatagramPacket p) {
		return new String(p.getData(), 0, p.getLength());
	}

	public boolean connect() throws IOException {
		String connResp = null;
		 NetworkRW adminNetworkRW = new TCPNetworkRW(new Socket(targetAddress, targetPort));
		int connAttepmt = 0;
		do {
			connAttepmt++;
			adminNetworkRW.writeToNetwork(START_MSG);
			LOG.fine("sent connection initator packet :" + START_MSG + " to " + targetAddress + ":" + targetPort);
			connResp = adminNetworkRW.readFromNetwork();
			LOG.fine("Server :" + connResp);
			if (connResp.equalsIgnoreCase(CONN_SUCCESS_MSG)) {
				adminNetworkRW.writeToNetwork(SET_PROTOCOL_TCP);
				connResp = adminNetworkRW.readFromNetwork();
				if(SUCCESS_SET_PROTOCOL_TCP.equalsIgnoreCase(connResp)) {
					adminNetworkRW.writeToNetwork("exit");
					adminNetworkRW.close();
					return true;
				}
			}
		} while (connAttepmt < MAX_CONN_ATTEMPT);

		return false;
	}
	
	public void close() throws IOException {
		LOG.fine("closing client packet :" + QUIT_MSG + " to " + targetAddress + ":" + targetPort);
		networkRW.writeToNetwork(QUIT_MSG);
		LOG.fine("closing client packet :" + CLOSE_MSG + " to " + targetAddress + ":" + targetPort);
		networkRW.writeToNetwork(CLOSE_MSG);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		/*System.out.println(System.getProperty("user.dir"));
		System.getProperties().put("Djava.util.logging.config.file", System.getProperty("user.dir")+"/logging.properties");*/
		LOG.finest("JAVA_HOME: "+System.getProperty("java.home"));
		LOG.finest("Logger initialized");
		LOG.finest("\n-------------------------------Starting UCIClient ---------------------------------------\n");
		ChessbaseClientAdapter adapter = new ChessbaseClientAdapter();
		adapter.listen();
	}
	
	private void tryExternalServerApproach() throws IOException {
		String localip = null;
		LOG.info("Trying to read IP from "+config.getProperty("external_server_ip"));
		String extServerUrl = config.getProperty("external_server_ip");
		LOG.fine("extServerUrl: "+extServerUrl);
		String ip = UCIUtil.getExternalParam(extServerUrl, "ip");
		localip = UCIUtil.getExternalParam(extServerUrl, "localip");			
		String adminPortStr = UCIUtil.getExternalParam(extServerUrl, "port");
		LOG.fine("ip: "+ip+"\tport: "+adminPortStr);
		adminPort = Integer.parseInt(adminPortStr);
		targetPort = adminPort + Integer.parseInt(config.getProperty("target_port"));
		targetAddress = InetAddress.getByName(ip);
		networkRW = new TCPNetworkRW(new Socket(targetAddress, targetPort));
		config.setProperty("target_ip", ip);
	}
	
	private void tryLocalIP() throws IOException {
		String localip = "192.168.1.11";		
		//check if local area network server is available
		InetAddress localAddress = InetAddress.getByName(localip);//server address is fixed at 192.168.1.11
		LOG.info("Trying with local ip");
		LOG.fine("localip: "+localip);
		targetAddress = InetAddress.getByName(localip);
		adminPort = Integer.parseInt(config.getProperty("admin_port"));
		targetPort = adminPort + Integer.parseInt(config.getProperty("target_port"));
		LOG.info("server: "+targetAddress.getHostName()+"\tport: "+targetPort);
		networkRW = new TCPNetworkRW(new Socket(targetAddress, targetPort));
	}
	
	private void tryManual() throws IOException {
		LOG.info("Trying to read from property file, expecting manual configuration");
		LOG.finest("adminPort: "+config.getProperty("admin_port"));
		LOG.finest("target_ip: "+config.getProperty("target_ip"));
		LOG.finest("target_port: "+config.getProperty("target_port"));
		adminPort = Integer.parseInt(config.getProperty("admin_port"));
		targetPort = adminPort + Integer.parseInt(config.getProperty("target_port"));
		targetAddress = InetAddress.getByName(config.getProperty("target_ip"));
		LOG.info("server: "+targetAddress.getHostName()+"\tport: "+targetPort);
		networkRW = new TCPNetworkRW(new Socket(targetAddress, targetPort));
	}

}
