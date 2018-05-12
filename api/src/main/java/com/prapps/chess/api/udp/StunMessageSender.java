package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.prapps.chess.api.StunServer;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.util.UtilityException;

public class StunMessageSender implements Runnable {
	private DatagramSocket socket;
	private AtomicBoolean exit;
	private AtomicBoolean connected;
	private AtomicReference<MessageHeader> sendMHRef;
	private StunServer stunServer;
	
	public StunMessageSender(DatagramSocket socket, AtomicBoolean exit, AtomicBoolean connected, AtomicReference<MessageHeader> sendMHRef, StunServer stunServer) {
		this.socket = socket;
		this.exit = exit;
		this.connected = connected;
		this.sendMHRef = sendMHRef;
		this.stunServer = stunServer;
	}
	
	@Override
	public void run() {
		int timeSinceFirstTransmission = 0;
		int timeout = 10000;
		while (!exit.get() && !connected.get()) {
			try {
				// Test 1 including response
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				sendMHRef.set(sendMH);
				
				ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);
				
				byte[] data = sendMH.getBytes();
				InetAddress stunAddress = InetAddress.getByName(stunServer.getHost());
				DatagramPacket send = new DatagramPacket(data, data.length, stunAddress, stunServer.getPort());
				synchronized (socket) {
					socket.connect(stunAddress, stunServer.getPort());
					socket.send(send);
					socket.disconnect();	
				}
				Thread.sleep(5000);
				System.out.println("stun packet sent");
			} catch (UtilityException | IOException | InterruptedException ste) {
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
			try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

}
