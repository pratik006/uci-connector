package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;

public class HandshakeSenderThread implements Runnable {
	private SharedContext ctx;
	
	public HandshakeSenderThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		Message msg;
		
		while (ctx.getNat().get() == null) {
			synchronized (ctx.getNat()) {
				try {
					ctx.getNat().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		NatDetail nat = ctx.getNat().get();
		while (!ctx.getExit().get()) {
			msg = new Message(Message.HANDSHAKE_TYPE);
			msg.setHost(nat.getHost());
			msg.setPort(nat.getPort());
			String json;
			try {
				json = ctx.getObjectMapper().writeValueAsString(msg);
				DatagramPacket p = new DatagramPacket(json.getBytes(), json.getBytes().length);
				if (msg.getHost() != null && msg.getPort() != 0) {
					p.setPort(msg.getPort());
					p.setAddress(InetAddress.getByName(msg.getHost()));
				}
				ctx.send(p);
				//System.out.println("handshake packet sent to "+p.getAddress().getHostName()+":"+p.getPort());
				if (ctx.getConnectionState().get().getState() == State.HANDSHAKE_ONE_WAY) {
					Thread.sleep(20000);					
				} else {
					Thread.sleep(1000);	
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			
			//try { Thread.sleep(36000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

}
