package com.prapps.chess.api.udp.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.State;

public class ConsoleReaderThread implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(ConsoleReaderThread.class);
	private SharedContext ctx;
	
	public ConsoleReaderThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		String line = null;
		try {
			while (ctx.getState() != State.HANDSHAKE_TWO_WAY) {
				synchronized (ctx.getConnectionState()) {
					ctx.getConnectionState().wait();
				}
			}
			
			LOG.debug("ConsoleReaderThread: listenening...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			while (!ctx.getExit().get()) {
				while (ctx.getState() != State.DISCONNECTED && (line = reader.readLine()) != null && !line.equals("\n")) {
					LOG.trace("line: "+line);
					try {
						Message msg = new Message(ctx.incrementSeq(), "critter", line + "\n");
						msg.setType(Message.ENGINE_TYPE);
						ctx.send(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (line.contains("quit")) {
						synchronized (ctx.getConnectionState()) {
							ctx.getConnectionState().get().setState(State.DISCONNECTED);
							ctx.getConnectionState().notifyAll();
						}
						break;
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		LOG.debug("exitting ConsoleReaderThread");
	}
}
