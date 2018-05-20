package com.prapps.chess.api.udp;

public class State {
	public static final int ACK = -1;
	public static final int DISCONNECTED = 0;
	public static final int MAC_UPDATED = 1;
	public static final int RECEIVED_OTHER_MAC = 2;
	public static final int RESET_CONNECTION = 3;
	public static final int HANDSHAKE_ONE_WAY = 4;
	public static final int HANDSHAKE_TWO_WAY = 5;
	
	public State() {
		this.state = DISCONNECTED;
	}
	
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public boolean isHigherState(int otherState) {
		return otherState > state;
	}
}
