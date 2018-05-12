package com.prapps.chess.server;

import com.prapps.chess.api.Message;

public interface EngineListener {
	void onMessage(Message msg);
}
