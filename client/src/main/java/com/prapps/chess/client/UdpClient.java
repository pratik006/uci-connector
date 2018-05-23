package com.prapps.chess.client;

import static com.prapps.chess.api.udp.State.RECEIVED_OTHER_MAC;

import java.io.ByteArrayInputStream;

import com.prapps.chess.api.udp.AbstractUdpBase;
import com.prapps.chess.api.udp.DatagramUciListener;
import com.prapps.chess.api.udp.State;
import com.prapps.chess.api.udp.thread.ConsoleReaderThread;
import com.prapps.chess.api.udp.thread.DatagramListenerThread;
import com.prapps.chess.api.udp.thread.GetOtherNatThread;
import com.prapps.chess.api.udp.thread.StateChangeThread;
import com.prapps.chess.api.udp.thread.StunMessageSender;
import com.prapps.chess.client.config.ClientConfig;
import com.prapps.chess.client.config.ConfigLoader;

public class UdpClient extends AbstractUdpBase {
	public UdpClient(ClientConfig config, boolean configOnly) throws Exception {
		super(config, configOnly);
		if (!configOnly)
			ctx.getListeners().add(new DatagramUciListener(ctx));
	}
	
	public static void main(String[] args) throws Exception {
		boolean config = false;
		if (args != null && args.length > 0 && args[0] != null) {
			config = "config".equalsIgnoreCase(args[0]);
		}
		UdpClient client = new UdpClient(ConfigLoader.INSTANCE.getClientConfig(), config);
		client.start();
	}
	
	public void start() {
		Thread datagramListener = new Thread(new DatagramListenerThread(ctx));datagramListener.start();
		Thread stateChangeThread = new Thread(new StateChangeThread(ctx));stateChangeThread.start();
		
		Thread stunSender = new Thread(new StunMessageSender(ctx));
		Thread otherNat = new Thread(new GetOtherNatThread(ctx));
		
		Thread consoleThread = new Thread(new ConsoleReaderThread(ctx));
		
		try {
			if (ctx.isConfigOnly() || System.currentTimeMillis() - ctx.getBaseConfig().getUdpConfig().getNat().getLastUpdated() > ctx.getBaseConfig().getTimeoutDuration()) {
				LOG.info("connection timeout.. re-establishing");
				stunSender.start();
				otherNat.start();
			} else {
				ctx.getNat().set(ctx.getBaseConfig().getUdpConfig().getNat());
				if (ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_TWO_WAY)) {
					synchronized (ctx.getConnectionState()) {
						ctx.getConnectionState().get().setState(State.HANDSHAKE_TWO_WAY);
						ctx.getConnectionState().notifyAll();
					}	
				}
			}
			
			if (!ctx.isConfigOnly()) {
				consoleThread.start();
				consoleThread.join();
				LOG.debug("joined consoleThread");
			}
			stateChangeThread.join();
			LOG.debug("joined stateChangeThread");
			consoleThread.interrupt();
			stunSender.interrupt();
			stunSender.join();
			LOG.debug("joined stunSender");
			otherNat.interrupt();
			otherNat.join();
			LOG.debug("joined otherNat");
			datagramListener.interrupt();
			ctx.close();
			datagramListener.join();
			LOG.debug("joined datagramListener");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			ctx.close();
		}
		LOG.debug("Exitting main thread");
		System.exit(0);
	}
}
