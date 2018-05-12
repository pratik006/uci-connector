package com.prapps.chess.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.udp.AbstractP2PListener;
import com.prapps.chess.api.udp.ConsoleReaderThread;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.HandshakeSenderThread;
import com.prapps.chess.api.udp.PacketListener;
import com.prapps.chess.api.udp.StunMessageListener;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.client.config.ClientConfig;
import com.prapps.chess.client.config.ConfigLoader;

import de.javawi.jstun.header.MessageHeader;

public class UdpTester {
	private static ObjectMapper mapper = new ObjectMapper();
	private static AtomicReference<MessageHeader> sendMHRef = new AtomicReference<MessageHeader>();
	private static ClientConfig clientConfig = ConfigLoader.INSTANCE.getServerConfig();
	private static DatagramSocket socket;
	private static AtomicBoolean connected = new AtomicBoolean(false);
	private static AtomicBoolean exit = new AtomicBoolean(false);
	private static AtomicReference<NatDetail> nat = new AtomicReference<>();
	
	public static void main(String[] args) throws SocketException {
		socket = new DatagramSocket(13000);
		socket.setReuseAddress(true);
		socket.setSoTimeout(5000);
		List<PacketListener> listeners = Arrays.asList(
				new StunMessageListener(sendMHRef, clientConfig.getExternalHost(), "lappy"), 
				new P2PMessageListener(socket, exit, connected)
			);
		
		DatagramListenerThread datagramListenerThread = new DatagramListenerThread(socket, exit, listeners);
		HandshakeSenderThread handshakeThread = new HandshakeSenderThread(socket, nat, exit, connected);
		GetOtherNatThread otherNatThread = new GetOtherNatThread(exit, nat, clientConfig.getExternalHost(), "Desky");
		StunMessageSender stunMessageSender = new StunMessageSender(socket, exit, connected, sendMHRef, clientConfig.getUdpConfig().getStunServers().get(clientConfig.getUdpConfig().getSelectedIndex()));

		Thread t5 = new Thread(datagramListenerThread);t5.start();
		Thread t2 = new Thread(stunMessageSender);t2.start();
		Thread t3 = new Thread(otherNatThread);t3.start();
		Thread t4 = new Thread(handshakeThread);t4.start();
		Thread consoleThread = new Thread(new ConsoleReaderThread(socket, exit, nat));consoleThread.start();
		
		try {
			consoleThread.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			consoleThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
	
	public static class P2PMessageListener extends AbstractP2PListener {
		public P2PMessageListener(DatagramSocket socket, AtomicBoolean exit, AtomicBoolean connected) {
			super(socket, exit, connected);
		}
		
		@Override
		public void onReceive(DatagramPacket packet) {
			Message msg;
			try {
				msg = mapper.readValue(new String(packet.getData()), Message.class);
				System.out.println(msg);
				if (msg.getType() == Message.ENGINE_TYPE) {
					/*if (seq+1 == msg.getSeq()) {
						handleMessage(msg);
						seq++;
						for (Message m : queue) {
							if (seq+1 == m.getSeq()) {
								handleMessage(m);
								seq++;
							}
						}
					} else {
						queue.add(msg);
					}*/	
				} else if (msg.getType() == Message.HANDSHAKE_TYPE) {
					super.onReceive(packet);
				}
			} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
