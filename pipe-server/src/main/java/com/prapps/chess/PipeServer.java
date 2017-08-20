package com.prapps.chess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PipeServer {
	private static final int BUF_SIZE = 1024*10;
	private Socket engineSocket;
	private ServerSocket serverSocket;
	private int port;
	
	public PipeServer(int port) throws IOException {
		this.port = port;
	}
	
	public void start() throws IOException {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			}catch(Exception ex) { }
		}
		serverSocket = new ServerSocket(port);
		engineSocket = serverSocket.accept();
	}
	
	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.load(new FileInputStream("pipeserver.ini"));
		PipeServer.startServer(prop);
	}
	
	public static void startServer(Properties prop) throws Exception {
		final int chessbaseStartPort = Integer.parseInt(prop.getProperty("chessbase-start-port"));
		final int engineStartPort = Integer.parseInt(prop.getProperty("engine-start-port"));
		final Map<Integer, PipeServer> servers = new HashMap<Integer, PipeServer>();
		
		for (int i=1; i<=5; i++) {
			final Integer port = i;
			new Thread(new Runnable() {
				public void run() {
					try {
						PipeServer server = servers.get(port);
						if (null != server) {
							try {
							server.close();
							}catch(Exception ex) { }
							servers.remove(port);
						}
						
						PipeServer pipeServer = new PipeServer(engineStartPort + port);
						System.out.println("starting at port:"+port);
						pipeServer.start();
						
						
						servers.put(port, pipeServer);
						System.out.println("Port: "+port+" is ready");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}).start();
		}
		
		final boolean stopFlag = false;
		for (int i=1; i<=5; i++) {
			final Integer port = i;
			new Thread(new Runnable() {
				public void run() {
					ServerSocket serverSocket = null;
					while (!stopFlag) {
						try {
							serverSocket = new ServerSocket(chessbaseStartPort + port);
							while (!stopFlag) {
								Socket socket = serverSocket.accept();
								System.out.println("connection accepted: " + port);
								PipeServer server = servers.get(port);
								server.connect(socket);
								System.out.println(socket);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								serverSocket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
		
	}
	
	public void connect(final Socket chessbaseSocket) throws Exception {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					pipe(engineSocket.getInputStream(), chessbaseSocket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					pipe(chessbaseSocket.getInputStream(), engineSocket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t2.start();
		t1.join();
		t2.join();
	}
	
	public void pipe(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[BUF_SIZE];
		int read = -1;
		while ((read = is.read(buf)) != -1) {
			os.write(buf, 0, read);
			os.flush();
			String data = new String(buf, 0, read);
			if (data.contains("quit")) {
				close();
				break;
			}
		}
	}
	
	public void close() throws IOException {
		engineSocket.close();
		serverSocket.close();
	}

}
