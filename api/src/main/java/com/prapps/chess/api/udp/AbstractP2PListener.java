package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;

public abstract class AbstractP2PListener implements PacketListener {
	protected ObjectMapper mapper = new ObjectMapper();
	protected long seq;
	
	protected AtomicBoolean exit;
	protected AtomicBoolean connected;
	protected DatagramSocket socket;
	
	public AbstractP2PListener(DatagramSocket socket, AtomicBoolean exit, AtomicBoolean connected) {
		this.exit = exit;
		this.connected = connected;
		this.socket = socket;
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		Message msg;
		try {
			msg = mapper.readValue(new String(packet.getData()), Message.class);
			if (msg.getType() == Message.HANDSHAKE_TYPE) {
				seq = 0;
				connected.set(true);
				socket.disconnect();
				System.out.println("connected..");
				synchronized (connected) {
					connected.notifyAll();	
				}
			}
		} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
