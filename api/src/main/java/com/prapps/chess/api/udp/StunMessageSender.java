package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.StunServer;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.util.UtilityException;

public class StunMessageSender implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(StunMessageSender.class);
	
	private SharedContext ctx;
	private StunServer stunServer;
	
	public StunMessageSender(SharedContext ctx) {
		this.ctx = ctx;
		stunServer = ctx.getBaseConfig().getUdpConfig().getStunServers().get(
				ctx.getBaseConfig().getUdpConfig().getSelectedIndex());
	}
	
	@Override
	public void run() {
		int timeSinceFirstTransmission = 0;
		int timeout = 10000;
		while (!ctx.getExit().get() && ctx.getConnectionState().get().getState() != State.HANDSHAKE_TWO_WAY) {
			try {
				// Test 1 including response
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				ctx.getSendMHRef().set(sendMH);
				
				ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);
				
				byte[] data = sendMH.getBytes();
				InetAddress stunAddress = InetAddress.getByName(stunServer.getHost());
				DatagramPacket send = new DatagramPacket(data, data.length, stunAddress, stunServer.getPort());
				ctx.connectAndSend(send);
				Thread.sleep(5000);
			} catch (UtilityException | IOException | InterruptedException ste) {
				if (timeSinceFirstTransmission < 7900) {
					LOG.error("Test 1: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = (timeSinceFirstTransmission * 2);
					if (timeoutAddValue > 1600) timeoutAddValue = 1600;
					timeout = timeoutAddValue;
				} else {
					// node is not capable of udp communication
					LOG.error("Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					LOG.error("Node is not capable of UDP communication.");
					
				}
			}
			try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

}
