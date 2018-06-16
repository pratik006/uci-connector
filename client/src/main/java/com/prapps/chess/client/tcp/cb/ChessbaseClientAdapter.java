package com.prapps.chess.client.tcp.cb;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.prapps.chess.tcp.common.ProtocolConstants;
import com.prapps.chess.tcp.common.ServerConfig;

public class ChessbaseClientAdapter {
	private static Logger LOG = Logger.getLogger(ChessbaseClientAdapter.class.getName());
	public static final int MAX_CONN_ATTEMPT = 5;
	
	private boolean exit = false;
	private InputStream consoleInputStream = System.in;
	private ClientConfig config;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ChessbaseClientAdapter adapter = new ChessbaseClientAdapter();
		adapter.start();
	}
	
	public void start() throws IOException, InterruptedException {
		config = ClientConfigLoader.INSTANCE.getConfig();
		log("Client config file loaded");
		ServerConfig.Engine engine = config.getServer().getEngines().iterator().next();
		InetAddress ip = InetAddress.getByName(config.getServer().getIp());
		Socket socket = new Socket(ip, engine.getOffsetPort());
		log("Socket created");
		Thread cbWriter = new Thread(new Runnable() {
			public void run() {
				StringBuilder serverMsg = null;
				while (!exit) {
					try {
						byte[] buf = new byte[ProtocolConstants.BUFFER_SIZE];
						int readLen = socket.getInputStream().read(buf);
						serverMsg = new StringBuilder();
						while (readLen != -1) {
							serverMsg.append(new String(buf, 0, readLen));
						}
						
						log("Server: "+serverMsg);
						if("exit".equalsIgnoreCase(serverMsg.toString())) {
							socket.close();
							exit = true;
						}
						else {
							System.out.write(serverMsg.toString().getBytes("UTF-8"));
							System.out.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				log("Closing CB Writer");
				try {
					System.out.write("quit".getBytes());
					consoleInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		Thread cbReader = new Thread(new Runnable() {
			public void run() {
				byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];
				try {
					int readLen = 0;
					while (!exit && (readLen = consoleInputStream.read(buffer)) != -1) {
						log(buffer, readLen);
						socket.getOutputStream().write(buffer, 0, readLen);
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
		log("Closing CB Adapter");
		System.exit(0);
	}
	
	private void log(String content) {
		log(content.getBytes(), content.getBytes().length);
	}
	
	private void log(byte[] buf, int len) {
		System.out.println("Chessbase: "+new String(buf, 0, len));
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Chessbase: "+new String(buf, 0, len));
		}
	}

}
