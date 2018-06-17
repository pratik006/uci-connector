package com.prapps.chess.server.uci.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.prapps.chess.server.uci.thread.AbstractRunnable;
import com.prapps.chess.server.uci.thread.State;


public class AsyncReader extends AbstractRunnable {

	private static Logger LOG = Logger.getLogger(AsyncReader.class.getName());
	
	private InputStream is;
	private volatile boolean stop = false;
	private OutputStream os;
	public volatile boolean exit;
	
	public AsyncReader(OutputStream os, InputStream is, Boolean exit) {
		this.os = os;
		this.is = is;
		this.exit = exit;
	}
	
	public void run() {
		setState(State.Running);
		try {
			byte[] buf = new byte[1024];
			int read = -1;
			while (!stop && State.Closed != getState() && (read = is.read(buf)) != -1) {
				LOG.finer("client: " + new String(buf, 0, read)+"\tLength: "+read);
				os.write(buf, 0, read);
				os.flush();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			stop = true;
			try {
				os.write("quit\n".getBytes());
				os.flush();
			} catch (IOException e1) { }	
		}/*
		finally {
			try {
				networkRW.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		LOG.info("Closing "+getClass().getName());
	}
	
	public void stop() {
		stop = true;
		setState(State.Closed);
	}
}
