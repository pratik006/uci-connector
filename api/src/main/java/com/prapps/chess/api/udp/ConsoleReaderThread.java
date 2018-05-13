package com.prapps.chess.api.udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;

public class ConsoleReaderThread implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(ConsoleReaderThread.class);
	private SharedContext ctx;
	
	public ConsoleReaderThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		String line = null;
		int seq = 1;
		try {
			while (ctx.getConnectionState().get().getState() != State.HANDSHAKE_TWO_WAY) {
				synchronized (ctx.getConnectionState()) {
					ctx.getConnectionState().wait();
				}
			}
			
			LOG.debug("ConsoleReaderThread: listenening...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			while (!ctx.getExit().get()) {
				while (!ctx.getExit().get() && (line = reader.readLine()) != null) {
					Message msg = new Message(seq++, "critter", line + "\n");
					msg.setHost(ctx.getNat().get().getHost());
					msg.setPort(ctx.getNat().get().getPort());
					byte[] buf;
					try {
						buf = ctx.getObjectMapper().writeValueAsString(msg).getBytes();
						DatagramPacket p = new DatagramPacket(buf, buf.length, 
								InetAddress.getByName(ctx.getNat().get().getHost()), ctx.getNat().get().getPort());
						ctx.send(p);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if ("quit".equalsIgnoreCase(line))
						ctx.getExit().set(true);
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		LOG.debug("exitting ConsoleReaderThread");
	}
}
