package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatagramListenerThread implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(StateChangeThread.class);
	private SharedContext ctx;
	
	public DatagramListenerThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		while (!ctx.getExit().get()) {
			byte[] buf = new byte[2000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			try {
				LOG.trace("waiting for packet...");
				ctx.receive(p);
				LOG.trace(new String("Received Packet from "+p.getAddress()+":"+p.getPort()+"\tDate: "+new String(p.getData())));
				ctx.getListeners().forEach(listener -> listener.onReceive(p));
			} catch(java.net.SocketTimeoutException e) { }
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}