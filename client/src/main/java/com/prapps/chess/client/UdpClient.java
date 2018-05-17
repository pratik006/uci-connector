package com.prapps.chess.client;

import java.io.IOException;
import java.net.DatagramPacket;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.udp.AbstractP2PListener;
import com.prapps.chess.api.udp.AbstractUdpBase;
import com.prapps.chess.api.udp.ConsoleReaderThread;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.StateChangeThread;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.client.config.ClientConfig;
import com.prapps.chess.client.config.ConfigLoader;

public class UdpClient extends AbstractUdpBase {
	public UdpClient(ClientConfig config) throws Exception {
		super(config);
		ctx.getListeners().add(new P2PMessageListener(ctx));
	}
	
	public static void main(String[] args) throws Exception {
		UdpClient client = new UdpClient(ConfigLoader.INSTANCE.getClientConfig());
		client.start();
	}
	
	public void start() {
		Thread datagramListener = new Thread(new DatagramListenerThread(ctx));datagramListener.start();
		Thread stunSender = new Thread(new StunMessageSender(ctx));stunSender.start();
		Thread otherNat = new Thread(new GetOtherNatThread(ctx));otherNat.start();
		Thread stateChangeThread = new Thread(new StateChangeThread(ctx));stateChangeThread.start();
		Thread consoleThread = new Thread(new ConsoleReaderThread(ctx));consoleThread.start();
		
		try {
			consoleThread.join();
			stunSender.join();
			otherNat.join();
			datagramListener.join();
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
