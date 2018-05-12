package com.prapps.chess.api.udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;

public class ConsoleReaderThread implements Runnable {
	private DatagramSocket socket;
	private AtomicBoolean exit;
	private AtomicReference<NatDetail> nat;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public ConsoleReaderThread(DatagramSocket socket, AtomicBoolean exit, AtomicReference<NatDetail> nat) {
		this.socket = socket;
		this.exit = exit;
		this.nat = nat;
	}
	
	@Override
	public void run() {
		String line = null;
		int seq = 1;
		try {
			while (nat.get() == null) {
				synchronized (nat) {
					nat.wait();
				}
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			while (!exit.get()) {
				while (!exit.get() && (line = reader.readLine()) != null) {
					Message msg = new Message(seq++, "critter", line + "\n");
					msg.setHost(nat.get().getHost());
					msg.setPort(nat.get().getPort());
					byte[] buf;
					try {
						buf = mapper.writeValueAsString(msg).getBytes();
						DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName(nat.get().getHost()), nat.get().getPort());
						socket.send(p);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if ("quit".equalsIgnoreCase(line))
						exit.set(true);
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("exit t1");
	}
}
