package com.prapps.chess.uci.share;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;



public class TCPNetworkRW implements NetworkRW {

	private static final Logger LOG = Logger.getLogger(TCPNetworkRW.class.getName());
	
	private Socket socket;
	
	public TCPNetworkRW(java.net.Socket socket) {
		this.socket = new Socket(socket);
	}
	
	public TCPNetworkRW(DatagramSocket datagramSocket, InetAddress address, int port) {
		LOG.finest("Creating socket at "+address+":"+port);
		this.socket = new Socket(datagramSocket, address, port);
	}
	
	public void writeToNetwork(String content) throws IOException {
		LOG.finest("Writing to Socket: "+content);
		socket.write(content);
	}
	
	public void writeToNetwork(byte[] content) throws IOException {
		LOG.finest("Writing to Socket: "+new String(content));
		socket.write(content);
	}

	public String readFromNetwork() throws IOException {
		String content = socket.read();
		LOG.finest("Read from socket: "+content);
		return content;
	}
	
	public InetAddress getAddress() {
		return socket.getAddress();
	}
	
	public int getPort() {
		return socket.getPort();
	}
	
	public void close() throws IOException {
		if(null != socket) {
			socket.close();
			LOG.finest("socket closed.");
		}
	}
	
	public boolean isConnected() {
		 return socket.isConnected();
	}
	
	public boolean isClosed() {
		 return socket.isClosed();
	}
}
