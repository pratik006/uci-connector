package com.prapps.chess.nat.udp;

import java.util.concurrent.atomic.AtomicLong;

import com.prapps.chess.api.udp.AbstractUdpBase;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.StateChangeThread;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.server.config.ServerConfig;

public class UdpListener extends AbstractUdpBase implements Runnable {
	public UdpListener(ServerConfig serverConfig) throws Exception {
		super(serverConfig);
		ctx.getListeners().add(new ServerDatagramP2pListener(ctx, serverConfig, new AtomicLong()));
	}

	@Override
	public void run() {
		try {
			Thread datagramListener = new Thread(new DatagramListenerThread(ctx));datagramListener.start();
			Thread stunSender = new Thread(new StunMessageSender(ctx));stunSender.start();
			Thread otherNat = new Thread(new GetOtherNatThread(ctx));otherNat.start();
			Thread stateChangeThread = new Thread(new StateChangeThread(ctx));stateChangeThread.start();
			
			stunSender.join();
			otherNat.join();
			datagramListener.join();
			stateChangeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
