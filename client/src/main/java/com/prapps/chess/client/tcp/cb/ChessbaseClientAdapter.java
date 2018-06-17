package com.prapps.chess.client.tcp.cb;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
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
	
	private Socket connect(ServerConfig serverConfig) throws UnknownHostException, IOException {
		Socket socket = null;
		int port = serverConfig.getEngines().iterator().next().getPort();
		if (serverConfig.getIp().equals(serverConfig.getLocalIp())) {
			try {
				log("connecting to "+serverConfig.getIp()+":"+port);
				socket = new Socket(serverConfig.getIp(), port);
			} catch(java.net.ConnectException ex) {
				log("Failed connecting to public ip");
			}	
		} else {
			log("connecting to "+serverConfig.getLocalIp()+":"+port);
			socket = new Socket(config.getServer().getLocalIp(), port);
		}
		
		
		return socket;
	}
	
	public void start() throws IOException, InterruptedException {
		config = ClientConfigLoader.INSTANCE.getConfig();
		log("Client config file loaded");
		final Socket socket = connect(config.getServer());
		
		log("Socket created");
		Thread cbWriter = new Thread(new Runnable() {
			public void run() {
				while (!exit) {
					try {
						byte[] buf = new byte[ProtocolConstants.BUFFER_SIZE];
						int readLen = -1;
						while ((readLen = socket.getInputStream().read(buf)) != -1) {
							System.out.write(buf, 0, readLen);
							System.out.flush();
						}
						
						log("Server: "+new String(buf, 0, readLen));
						if("exit".equalsIgnoreCase(new String(buf, 0, readLen))) {
							exit = true;
						} else {
							System.out.write(buf, 0, readLen);
							System.out.flush();
						}
					} catch (IOException e) {
						exit = true;
						e.printStackTrace();
					}
				}
				
				try {
					System.out.write("quit\n".getBytes());
					//consoleInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				log("Closing CB Writer");
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
						socket.getOutputStream().flush();
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
		
		log("cbWriter joined");
		
		cbReader.stop();
		/* TODO cbReader.interrupt instead of stop
		 * if (cbReader.isAlive())
			cbReader.join();*/
		log("cbReader joined");
		
		log("Closing Chessbase Client Adapter");
		socket.close();
		System.exit(0);
	}
	
	private void log(String content) {
		log(content.getBytes(), content.getBytes().length);
	}
	
	private void log(byte[] buf, int len) {
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Chessbase: "+new String(buf, 0, len));
		}
	}

}
