package com.prapps.chess.server.uci.thread;

import java.util.logging.Logger;

public abstract class AbstractRunnable implements Runnable {

	protected Logger LOG = Logger.getLogger(AbstractRunnable.class.getName());
	private State state;
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		LOG.info("state: "+state);
		this.state = state;
	}
}
