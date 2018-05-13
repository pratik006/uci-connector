package com.prapps.chess.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.api.udp.AbstractP2PListener;
import com.prapps.chess.api.udp.ConsoleReaderThread;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.State;
import com.prapps.chess.api.udp.StateChangeThread;
import com.prapps.chess.api.udp.StunMessageListener;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.client.config.ConfigLoader;

import de.javawi.jstun.header.MessageHeader;

public class UdpClient {
	private SharedContext ctx;
	
	public UdpClient() throws SocketException {
		DatagramSocket socket = new DatagramSocket(13000);
		socket.setReuseAddress(true);
		socket.setSoTimeout(5000);
		ctx = new SharedContext();
		ctx.setSocket(socket);
		ctx.setId("lappy");
		ctx.setBaseConfig(ConfigLoader.INSTANCE.getClientConfig());
		ctx.setExit(new AtomicBoolean(false));
		ctx.setNat(new AtomicReference<>());
		ctx.setSendMHRef(new AtomicReference<MessageHeader>());
		ctx.setConnectionState(new AtomicReference<State>(new State()));
		ctx.setListeners(Arrays.asList(
				new StunMessageListener(ctx), 
				new P2PMessageListener(ctx)
			));
		ctx.setObjectMapper(new ObjectMapper());
	}
	
	public static void main(String[] args) throws SocketException {
		UdpClient client = new UdpClient();
		client.start();
	}
	
	public void start() {
		DatagramListenerThread datagramListenerThread = new DatagramListenerThread(ctx);
		GetOtherNatThread otherNatThread = new GetOtherNatThread(ctx);
		StunMessageSender stunMessageSender = new StunMessageSender(ctx);
		StateChangeThread stateChange = new StateChangeThread(ctx);

		Thread t5 = new Thread(datagramListenerThread);t5.start();
		Thread t2 = new Thread(stunMessageSender);t2.start();
		Thread t3 = new Thread(otherNatThread);t3.start();
		//Thread t4 = new Thread(handshakeThread);t4.start();
		Thread stateChangeThread = new Thread(stateChange);stateChangeThread.start();
		Thread consoleThread = new Thread(new ConsoleReaderThread(ctx));consoleThread.start();
		
		try {
			consoleThread.join();
			t2.join();
			t3.join();
			//t4.join();
			t5.join();
			consoleThread.join();
			stateChangeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			ctx.close();
		}
	}
	
	public static class P2PMessageListener extends AbstractP2PListener {
		public P2PMessageListener(SharedContext ctx) {
			super(ctx);
		}
		
		@Override
		public void onReceive(DatagramPacket packet) {
			Message msg;
			try {
				msg = ctx.getObjectMapper().readValue(new String(packet.getData()), Message.class);
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
				} else {
					super.onReceive(packet);
				}
			} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
