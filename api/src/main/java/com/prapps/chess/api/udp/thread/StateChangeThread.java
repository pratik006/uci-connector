package com.prapps.chess.api.udp.thread;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.State;

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
			while (!ctx.getExit().get() && ctx.getConnectionState().get().getState() == prevState) {
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
				Message msg = new Message(ctx.incrementSeq(), ctx.getBaseConfig().getSelectedEngine(), "uci" + "\n");
				msg.setType(Message.ENGINE_TYPE);
				try {
					ctx.send(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (ctx.isConfigOnly()) {
					synchronized (ctx.getExit()) {
						ctx.getExit().set(Boolean.TRUE);
						ctx.getExit().notifyAll();
					}
				}
			break;
			case State.DISCONNECTED:
				LOG.info("Exit initiated...");
				synchronized (ctx.getExit()) {
					ctx.getExit().set(Boolean.TRUE);
					ctx.getExit().notifyAll();
				}
			break;
			}			
		}
		LOG.info("exitting...");
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
