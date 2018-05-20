package com.prapps.chess.client;

import com.prapps.chess.api.udp.AbstractUdpBase;
import com.prapps.chess.api.udp.DatagramUciListener;
import com.prapps.chess.api.udp.thread.ConsoleReaderThread;
import com.prapps.chess.api.udp.thread.DatagramListenerThread;
import com.prapps.chess.api.udp.thread.GetOtherNatThread;
import com.prapps.chess.api.udp.thread.StateChangeThread;
import com.prapps.chess.api.udp.thread.StunMessageSender;
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
			stateChangeThread.join();
			consoleThread.interrupt();
			consoleThread.join();
			stunSender.interrupt();
			stunSender.join();
			otherNat.interrupt();
			otherNat.join();
			datagramListener.interrupt();
			datagramListener.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			ctx.close();
		}
		LOG.debug("Exitting main thread");
		System.exit(0);
	}
}
