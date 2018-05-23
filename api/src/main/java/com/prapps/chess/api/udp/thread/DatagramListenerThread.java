package com.prapps.chess.api.udp.thread;

import java.io.IOException;
import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.udp.SharedContext;

public class DatagramListenerThread implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(DatagramListenerThread.class);
	private SharedContext ctx;
	
	public DatagramListenerThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		while (!ctx.getExit().get()) {
			byte[] buf = new byte[65000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			try {
				ctx.receive(p);
				LOG.trace(new String("Received Packet from "+p.getAddress()+":"+p.getPort()+"\tData: "+new String(p.getData())));
				new Thread(() -> ctx.getListeners().forEach(listener -> listener.onReceive(p))).start();
			} catch(java.net.SocketTimeoutException e) { e.printStackTrace(); }
			catch (IOException e) {
				if (!ctx.getExit().get())
					e.printStackTrace();
			}
		}
		LOG.debug("exitting DatagramListenerThread");
	}
}
