package com.prapps.chess.api.udp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;

public class StateChangeThread implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(StateChangeThread.class);
	private SharedContext ctx;
	
	public StateChangeThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		int prevState = State.DISCONNECTED;
		
		while (!ctx.getExit().get()) {
			while (ctx.getConnectionState().get().getState() == prevState) {
				synchronized (ctx.getConnectionState()) {
					try {
						ctx.getConnectionState().wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}
			
			//state changed
			prevState = ctx.getConnectionState().get().getState();
			LOG.debug("Current State: "+ctx.getConnectionState().get().getState());
			switch (ctx.getConnectionState().get().getState()) {
			case State.MAC_UPDATED: LOG.debug("Mac address updated");
			break;
			case State.RECEIVED_OTHER_MAC:
				LOG.debug("Recvd Other Nat "+ctx.getNat());
				while (ctx.getConnectionState().get().getState() == State.RECEIVED_OTHER_MAC) {
					try {
						ctx.send(Message.HANDSHAKE_TYPE);
						Thread.sleep(1000);	
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}	
				}
			break;
			case State.HANDSHAKE_ONE_WAY:
				LOG.debug("one way handshake");
				while (ctx.getConnectionState().get().getState() == State.HANDSHAKE_ONE_WAY) {
					try {
						ctx.send(Message.HANDSHAKE_COMNPLETE_TYPE);
						Thread.sleep(1000);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}				
			break;
			case State.HANDSHAKE_TWO_WAY:
				LOG.debug("Handshake complete");
			break;
			}
			
		}
	}
}
