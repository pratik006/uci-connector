package com.prapps.chess.tcp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class ChessEngine {
	private static Logger LOG = Logger.getLogger(ChessEngine.class.getName());
	
	private static final int BUF_SIZE = 1024*10;
	public static String QUIT_CMD = "quit\r\n";

	private OutputStream pos;
	private InputStream pis;
	private Process process;
	private String path;
	private String command;
	private Socket pipeServerSocket;

	public ChessEngine(String enginePath, String command) throws IOException {
		this.path = enginePath;
		this.command = command;
	}
	
	public boolean isReady() throws Exception {
		byte[] buf = new byte[1024*10];
		int read = -1;
		String content = null;
		read = pis.read(buf);
		content = new String(buf);
		System.out.println(content);
		
		pos.write("isready\r\n".getBytes());
		pos.flush();
		Thread.sleep(1000);
		
		read = pis.read(buf);
		content = new String(buf, 0, read);
		if (read != -1 && content.contains("readyok")) {
			return true;
		}
		
		return false;
	}

	public void start() throws IOException {
		Process p = null;
		LOG.fine("\n--------------------------------------Start Engine ----------------------------------\n");
		if (null == command || "".equals(command)) {
			p = Runtime.getRuntime().exec(path);
		} else {
			p = new ProcessBuilder(path, command).start();
		}

		pis = p.getInputStream();
		pos = p.getOutputStream();
		process = p;
	}

	public void stop() throws IOException {
		process.getOutputStream().write(QUIT_CMD.getBytes());
		pos.close();
		pis.close();
		process.destroy();
	}
	
	public void restart() throws IOException {
		stop();
		start();
	}

	public OutputStream getPos() {
		return pos;
	}
	
	public void connect(final Socket socket) throws Exception {
		if (null == pipeServerSocket) {
			pipeServerSocket = socket;
		}
		start();
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					pipe(socket.getInputStream(), pos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				try {
					pipe(pis, socket.getOutputStream());
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
			System.out.println("cmd: "+new String(buf, 0, read));
			os.write(buf, 0, read);
			os.flush();
			if (new String(buf).contains("quit")) {
				pipeServerSocket.close();
				break;
			}
		}
	}

	public static void main(String args[]) throws Exception {
		ChessEngine cb = new ChessEngine(
				"/media/pratik/Backup/programming/Chess/ChessEngines/stockfish-dd-src/stockfish", null);
		System.out.println(cb.isReady());
		/*String data = cb.read();
		System.out.println(data);
		cb.write("uci\r\n");
		Thread.sleep(300);
		data = cb.read();
		System.out.println(data);*/
	}
}
