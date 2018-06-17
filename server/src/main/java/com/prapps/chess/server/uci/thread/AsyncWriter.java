package com.prapps.chess.server.uci.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.prapps.chess.server.uci.thread.AbstractRunnable;
import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.tcp.common.ProtocolConstants;

public class AsyncWriter extends AbstractRunnable {

	private static Logger LOG = Logger.getLogger(AsyncWriter.class.getName());
	
	private InputStream is;
	private OutputStream os;

	public AsyncWriter(InputStream is, OutputStream os, Boolean exit) {
		this.os = os;
		this.is = is;
	}

	public void run() {
		byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE/2];
		try {
			int readLen = -1;
			while(State.Closed != getState() && (readLen = is.read(buffer, 0, buffer.length)) != -1) {
				LOG.finer("Server: "+new String(buffer, 0, readLen));
				os.write(buffer, 0, readLen);
				os.flush();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		LOG.info("Engine process closed");
		try {
			//if(networkRW.isConnected() && !networkRW.isClosed())
				os.write("exit".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*finally {
			try {
				networkRW.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		LOG.info("closing writer");
	}
	
	public void stop() {
	}
}
