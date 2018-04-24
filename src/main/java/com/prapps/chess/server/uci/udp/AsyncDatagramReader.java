package com.prapps.chess.server.uci.udp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.UCIUtil;

public class AsyncDatagramReader implements Runnable {

	private static Logger LOG = Logger.getLogger(AsyncDatagramReader.class.getName());
	
	private DatagramSocket socket;
	private volatile boolean stop = false;
	private OutputStream os;
	private byte[] password;
	
	public AsyncDatagramReader(OutputStream os, DatagramSocket socket,DatagramPacket packet, Boolean exit) {
		this.os = os;
		this.socket = socket;
	}
	
	public AsyncDatagramReader(OutputStream os, DatagramSocket socket,DatagramPacket packet, Boolean exit, byte[] password) {
		this.os = os;
		this.socket = socket;
		this.password = password;
	}
	
	public void run() {
		byte[] buf = new byte[ProtocolConstants.BUFFER_SIZE];
		StringBuffer message = new StringBuffer();
		while (!stop) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				message.append(UCIUtil.readPacket(packet, password));
				if(message.lastIndexOf("\n") != message.length()-1) {
					message.append("\n");
				}
				LOG.info("Chessbase: " + message);
				os.write(message.toString().getBytes());
				os.flush();
				if (message.indexOf("quit") != -1) {
					stop = true;
				}
				message = new StringBuffer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		 LOG.info("Closing "+getClass().getName());
	}
	
	public void stop() {
		stop = true;
	}
}
