package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;

public class HandshakeSenderThread implements Runnable {
	private DatagramSocket socket;
	private AtomicReference<NatDetail> natRef;
	private ObjectMapper mapper = new ObjectMapper();
	private AtomicBoolean connected;
	private AtomicBoolean exit;
	
	public HandshakeSenderThread(DatagramSocket socket, AtomicReference<NatDetail> natRef, AtomicBoolean exit, AtomicBoolean concected) {
		this.socket = socket;
		this.natRef = natRef;
		this.exit = exit;
		this.connected = concected;
	}
	
	@Override
	public void run() {
		Message msg;
		
		while (natRef.get() == null) {
			synchronized (natRef) {
				try {
					natRef.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		NatDetail nat = natRef.get();
		while (!exit.get()) {
			while (!connected.get()) {
				msg = new Message(Message.HANDSHAKE_TYPE);
				msg.setHost(nat.getHost());
				msg.setPort(nat.getPort());
				String json;
				try {
					json = mapper.writeValueAsString(msg);
					DatagramPacket p = new DatagramPacket(json.getBytes(), json.getBytes().length);
					if (msg.getHost() != null && msg.getPort() != 0) {
						p.setPort(msg.getPort());
						p.setAddress(InetAddress.getByName(msg.getHost()));
					}
					socket.send(p);
					System.out.println("handshake packet sent to "+p.getAddress().getHostName()+":"+p.getPort());
					Thread.sleep(1000);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}	
			}
			try { Thread.sleep(36000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

}
