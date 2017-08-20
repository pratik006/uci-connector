package com.prapps.chess.client.tcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

public class ChessbaseClientAdapter {
	private static Logger LOG = Logger.getLogger(ChessbaseClientAdapter.class.getName());
	
	private static final int BUF_SIZE = 1024*10;
	
	public static final int MAX_CONN_ATTEMPT = 5;
	
	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.load(new FileInputStream("client-config.ini"));
		final int cbStartPort = Integer.parseInt(prop.getProperty("chessbase-start-port"));
		final int enginePort = Integer.parseInt(prop.getProperty("engine-port"));
		final InetAddress ip = InetAddress.getByName("localhost");
		final ChessbaseClientAdapter adapter = new ChessbaseClientAdapter();
		
		new Thread(new Runnable() {
			public void run() {
				try {
					Socket socket = new Socket(ip, (int)(cbStartPort + enginePort));
					adapter.connect(socket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void connect(final Socket chessbaseSocket) throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					pipe(System.in, chessbaseSocket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					pipe(chessbaseSocket.getInputStream(), System.out);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}).start();
	}
	
	public void pipe(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[BUF_SIZE];
		int read = -1;
		while ((read = is.read(buf)) != -1) {
			os.write(buf, 0, read);
			os.flush();
			if (new String(buf).contains("quit")) {
				System.exit(0);
			}
		}
	}

}
