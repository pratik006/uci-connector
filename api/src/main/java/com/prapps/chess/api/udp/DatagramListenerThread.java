package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatagramListenerThread implements Runnable {
	private DatagramSocket socket;
	private  AtomicBoolean exit;
	private List<PacketListener> listeners;
	
	public DatagramListenerThread(DatagramSocket socket, AtomicBoolean exit, List<PacketListener> listeners) {
		this.socket = socket;
		this.exit = exit;
		this.listeners = listeners;
	}
	
	@Override
	public void run() {
		while (!exit.get()) {
			byte[] buf = new byte[2000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(p);
				listeners.forEach(listener -> listener.onReceive(p));
			} catch(java.net.SocketTimeoutException e) { }
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
