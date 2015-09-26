package com.prapps.chess.server.uci.thread;

public enum State {

	New,
	Waiting,
	Connected,
	Running,
	Stopping,
	Closed,
	Exit;
}
