package com.prapps.chess.client;

import com.prapps.chess.api.udp.AbstractUdpBase;
import com.prapps.chess.api.udp.ConsoleReaderThread;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.DatagramUciListener;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.StateChangeThread;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.client.config.ClientConfig;
import com.prapps.chess.client.config.ConfigLoader;

public class UdpClient extends AbstractUdpBase {
	public UdpClient(ClientConfig config) throws Exception {
		super(config);
		ctx.getListeners().add(new DatagramUciListener(ctx));
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
}
