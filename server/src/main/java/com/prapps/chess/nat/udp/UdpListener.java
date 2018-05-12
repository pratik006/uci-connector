package com.prapps.chess.nat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.udp.AbstractP2PListener;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.HandshakeSenderThread;
import com.prapps.chess.api.udp.PacketListener;
import com.prapps.chess.api.udp.StunMessageListener;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.nat.AbstractNetworkListener;
import com.prapps.chess.server.config.ServerConfig;

import de.javawi.jstun.header.MessageHeader;

public class UdpListener extends AbstractNetworkListener implements Runnable {
	private DatagramSocket socket;
	
	private static InetAddress srcAddress;
	private int srcPort;
	private InetAddress targetAddress;
	private int targetPort;
	
	private Map<String, Long> sequenceMap = new HashMap<>();
	
	private List<PacketListener> listeners;
	private AtomicBoolean exit = new AtomicBoolean(false);
	private AtomicBoolean connected = new AtomicBoolean(false);
	private AtomicReference<MessageHeader> sendMH = new AtomicReference<MessageHeader>();
	private static AtomicReference<NatDetail> natRef = new AtomicReference<NatDetail>();
	
	private long seq;
	private PriorityQueue<Message> queue = new PriorityQueue<>();
	
	public UdpListener(AtomicBoolean exit, ServerConfig serverConfig) throws ClassNotFoundException, SocketException, UnknownHostException {
		super(exit, serverConfig);
		this.srcPort = serverConfig.getUdpConfig().getSourcePort();
		//srcAddress = InetAddress.getByName("localhost");
		srcAddress = getLocalAddress();
		listeners = new ArrayList<>();
		listeners.add(new StunMessageListener(sendMH, serverConfig.getExternalHost(), "Desky"));
		listeners.add(new AbstractP2PListener(socket, exit, connected) {
			@Override
			public void onReceive(DatagramPacket packet) {
				Message msg;
				try {
					msg = mapper.readValue(new String(packet.getData()), Message.class);
					System.out.println(msg);
					if (msg.getType() == Message.ENGINE_TYPE) {
						if (seq+1 == msg.getSeq()) {
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
						}	
					} else if (msg.getType() == Message.HANDSHAKE_TYPE) {
						super.onReceive(packet);
					} else if (msg.getType() == Message.AVAILABLE_ENGINE_TYPE) {
						send(new Message(1, null, Arrays.toString(engineIds.toArray())));
					}
				} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void run() {
		try {
			socket = new DatagramSocket(new InetSocketAddress(srcAddress, srcPort));
			socket.setSoTimeout(10000);
			socket.setReuseAddress(true);
			Thread stunSender = new Thread(new StunMessageSender(socket, exit, connected, sendMH, serverConfig.getUdpConfig().getStunServers().get(serverConfig.getUdpConfig().getSelectedIndex())));stunSender.start();
			Thread otherNatThread = new Thread(new GetOtherNatThread(connected, natRef, serverConfig.getExternalHost(), "lappy"));otherNatThread.start();
			Thread datagramListener = new Thread(new DatagramListenerThread(socket, exit, listeners));datagramListener.start();
			Thread handshakeerThread = new Thread(new HandshakeSenderThread(socket, natRef, exit, connected));handshakeerThread.start();
			
			stunSender.join();
			otherNatThread.join();
			datagramListener.join();
			handshakeerThread.join();
		} catch (SocketException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void send(Message msg) {
		if (!sequenceMap.containsKey(targetAddress.getHostName())) {
			sequenceMap.put(targetAddress.getHostName(), 0l);
		}
		long seq = sequenceMap.get(targetAddress.getHostName());
		msg.setSeq(seq);
		//System.out.println("to be sent: "+new String(msg.getData()));
		byte[] buf;
		try {
			buf = mapper.writeValueAsString(msg).getBytes();
			DatagramPacket p = new DatagramPacket(buf, buf.length, targetAddress, targetPort);
			socket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
