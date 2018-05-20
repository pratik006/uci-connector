package com.prapps.chess.api.udp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				sendMsg(State.RESET_CONNECTION);
			break;
			case State.RESET_CONNECTION:
				ctx.resetSeq();
				ctx.resetUuid();
				LOG.debug("Connection Reset");
				sendMsg(State.HANDSHAKE_ONE_WAY);
			break;
			case State.HANDSHAKE_ONE_WAY:
				LOG.debug("one way handshake");
				sendMsg(State.HANDSHAKE_TWO_WAY);
			break;
			case State.HANDSHAKE_TWO_WAY:
				LOG.info("Handshake complete");
			break;
			}
			
		}
	}
	
	public void sendMsg(int state) {
		while (ctx.getConnectionState().get().getState() == state-1) {
			try {
				ctx.send(state);
				Thread.sleep(500);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
}
