package com.prapps.chess.nat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.prapps.chess.server.config.ServerConfig;
import com.prapps.chess.server.config.ServerConfig.StunServer;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.util.UtilityException;

public class MacAddressUpdaterThread implements Runnable {

	private DatagramSocket dgSocket;
	private AtomicBoolean exit;
	private StunServer stunServer;
	private int interval;
	private AtomicReference<MessageHeader> sendMH;
	
	public MacAddressUpdaterThread(AtomicBoolean exit, DatagramSocket socket, ServerConfig serverConfig, AtomicReference<MessageHeader> sendMH) {
		this.exit = exit;
		this.dgSocket = socket;
		this.stunServer = serverConfig.getUdpConfig().getStunServers().get(serverConfig.getUdpConfig().getSelectedIndex());
		this.interval = serverConfig.getUdpConfig().getRefreshInterval();
		this.sendMH = sendMH;
	}
	
	@Override
	public void run() {
		int timeSinceFirstTransmission = 0;
		int timeout = 10000;
		while (!exit.get()) {
			try {
				// Test 1 including response
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				this.sendMH.set(sendMH);
				
				ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);
				
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(stunServer.getHost()), stunServer.getPort());
				//dgSocket.connect(InetAddress.getByName(stunServer.getHost()), stunServer.getPort());
				dgSocket.send(send);
			} catch (UtilityException | IOException ste) {
				if (timeSinceFirstTransmission < 7900) {
					System.out.println("Test 1: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = (timeSinceFirstTransmission * 2);
					if (timeoutAddValue > 1600) timeoutAddValue = 1600;
					timeout = timeoutAddValue;
				} else {
					// node is not capable of udp communication
					System.out.println("Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					System.out.println("Node is not capable of UDP communication.");
					
				}
			}
			try { Thread.sleep(interval); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
