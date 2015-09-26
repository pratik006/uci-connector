package com.prapps.chess.uci.share;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Logger;

public class Socket {
	
	private static Logger LOG = Logger.getLogger(Socket.class.getName());

	private java.net.Socket socket;
	private DatagramSocket datagramSocket;
	private InetAddress address;
	private int port;
	
	public Socket(java.net.Socket socket) {
		this.socket = socket;
	}
	
	public Socket(DatagramSocket datagramSocket, InetAddress address, int port) {
		this.datagramSocket = datagramSocket;
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		if(null != socket)
			return socket.getInetAddress();
		else if(null != datagramSocket)
			return address;
		return null;
	}
	
	public int getPort() {
		if(null != socket)
			return socket.getPort();
		else if(null != datagramSocket)
			return datagramSocket.getPort();
		return -1;
	}
	
	public void write(String content) throws IOException {
		write(content.getBytes());
	}
	
	public void write(byte[] content) throws IOException {
		LOG.finest(Socket.class.getName()+" -> write");
		LOG.finest("content -> "+new String(content));
		if(null != socket) {
			LOG.finest("writing to socket "+socket.getLocalAddress()+ ":" + socket.getLocalPort());
			socket.getOutputStream().write(content);
			socket.getOutputStream().flush();
		}
		else if(null != datagramSocket) {
			LOG.finest("sending datagram packet to "+address + ":" + port);
			DatagramPacket newPacket = new DatagramPacket(content, content.length, address, port);
			datagramSocket.send(newPacket);
		}
	}
	
	public String read() throws IOException  {
		LOG.finest(Socket.class.getName()+" -> read");
		StringBuffer sb = new StringBuffer();
		byte[] buf = new byte[ProtocolConstants.BUFFER_SIZE];
		if(socket != null) {
			LOG.finest("reading from socket -> "+socket.getLocalAddress()+":"+socket.getLocalPort());
			int readLen = buf.length;
			try {
				while (readLen == buf.length && (readLen = socket.getInputStream().read(buf, 0, buf.length)) != 0) {
					if(readLen != -1) {
						LOG.finest("readLen: "+readLen);
						sb.append(new String(buf, 0, readLen));
					}
					else {
						LOG.finest("end of stream");
						return null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			LOG.finest("returning -> "+sb.toString()+"\tLength -> "+sb.toString().length());
			return sb.toString();
		}
		else if(datagramSocket != null) {
			LOG.finest("reading datagram packet -> "+address+":"+port);
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			datagramSocket.receive(packet);
			sb.append(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
			LOG.finest("returning -> "+sb.toString()+"\tLength -> "+sb.toString().length());
			return sb.toString();
		}
		return null;
	}
	
	public void close() throws IOException {
		if(socket.isConnected())
			socket.close();
	}
	
	public boolean isConnected() {
		return socket.isConnected();
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
}
