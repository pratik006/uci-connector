package com.prapps.chess.uci.share;

import java.io.IOException;
import java.net.InetAddress;


public interface NetworkRW {
	void writeToNetwork(String content) throws IOException;
	void writeToNetwork(byte[] content) throws IOException;
	String readFromNetwork() throws IOException;
	public InetAddress getAddress();
	public int getPort();
	public void close() throws IOException;
	public boolean isConnected();
	boolean isClosed();
}
