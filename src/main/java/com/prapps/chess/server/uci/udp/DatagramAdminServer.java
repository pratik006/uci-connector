package com.prapps.chess.server.uci.udp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;

import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.UCIUtil;

public class DatagramAdminServer implements ProtocolConstants {

	private DatagramSocket datagramSocket = null;
	private Properties config;
	private boolean exit = false;
	private String password;
	
	public DatagramAdminServer() throws IOException {
		config = new Properties();
		config.load(new FileInputStream("config.ini"));
		datagramSocket = new DatagramSocket(DEFAULT_ADMIN_PORT, InetAddress.getByName("127.0.0.1"));	
		System.out.println("Port number: "+DEFAULT_ADMIN_PORT+" "+datagramSocket.getLocalSocketAddress());
	}

	public void start() {		
		byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		String response = null;
		String selectedEngine = null;
		String command = null;
		String[] params = null;
		while(!exit) {
			try {				
				datagramSocket.receive(packet);
				System.out.println("Connection received from "+ packet.getAddress().getHostName());				
				response = UCIUtil.readPacket(packet, password.getBytes());
				params = UCIUtil.getMessageParams(response);
				command = params[0];
				System.out.println(response);
				if(GET_AVAILABLE_ENGINES.equals(command)) {
					UCIUtil.sendPacket(config.getProperty(AVAILABLE_ENGINES).getBytes(UCIUtil.CHARSET), password.getBytes() ,datagramSocket, packet.getAddress(), packet.getPort());
				}
				else if(SELECTED_ENGINE.equals(command)) {
					selectedEngine = params[1];
					UCIUtil.sendPacket((SUCCESSFUL_ENGINE_SELECTION_MESSAGE+DELIMITER+config.getProperty(selectedEngine)).getBytes(), password.getBytes() ,datagramSocket, packet.getAddress(), packet.getPort());
				}
				else {
					UCIUtil.sendPacket(UNKNOWN_COMMAND.getBytes(), password.getBytes() ,datagramSocket, packet.getAddress(), packet.getPort());
					System.out.println("Closed");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendHelloPacket() {
		byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
		packet.setPort(DEFAULT_ADMIN_PORT);
		packet.setAddress(datagramSocket.getInetAddress());
		System.out.println(datagramSocket.getLocalAddress());
		UCIUtil.sendPacket(HELLO_PACKET.getBytes(), password.getBytes() ,datagramSocket, datagramSocket.getLocalAddress(), DEFAULT_ADMIN_PORT);
		packet = new DatagramPacket(buffer, buffer.length);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		DatagramAdminServer server = new DatagramAdminServer();
		//server.start();
		server.sendHelloPacket();
	}
	
	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

}
