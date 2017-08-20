package com.prapps.chess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PipeServer {
	private static Logger LOG = Logger.getLogger(PipeServer.class);
	
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
		System.out.println("Listening on : "+port);
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
		
		for (int i=1; i<=5; i++) {
			final Integer port = i;
			new Thread(new Runnable() {
				public void run() {
					while(true) {
						LOG.info("starting at port:"+(engineStartPort+port));
						try {
							final PipeServer pipeServer = new PipeServer(engineStartPort + port);
							pipeServer.start();
							LOG.trace("Port: "+port+" is ready");
							
							
							Thread cbt = new Thread(new Runnable() {
								public void run() {
									ServerSocket serverSocket = null;
									//while (true) {
										try {
											int targetPort = chessbaseStartPort + port;
											serverSocket = new ServerSocket(targetPort);
											Socket socket = serverSocket.accept();
											LOG.trace("connection accepted: " + port);
											pipeServer.connect(socket);
											LOG.trace(socket);
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											try {
												serverSocket.close();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									//}
								}
							});
							cbt.start();
							cbt.join();
						} catch (Exception e) {
							e.printStackTrace();
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
					LOG.info("Socket Closed");
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
					LOG.info("Socket Closed");
				}
			}
		});
		t2.start();
		t1.join();
		t2.join();
		System.out.println("connection closed");
	}
	
	public void pipe(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[BUF_SIZE];
		int read = -1;
		while ((read = is.read(buf)) != -1) {
			os.write(buf, 0, read);
			os.flush();
			String data = new String(buf, 0, read);
			LOG.trace(data);
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
