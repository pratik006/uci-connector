package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.prapps.chess.api.RestUtil;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class AbstractNetworkBase {

	public static InetAddress getLocalAddress() throws ClassNotFoundException, SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
			while (iaddresses.hasMoreElements() && iface.getDisplayName().indexOf("docker") == -1) {
				InetAddress iaddress = iaddresses.nextElement();
				if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
					if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
						return iaddress;
					}
				}
			}
		}
		
		return null;
	}
	
	protected static MappedAddress updateMacAddress(DatagramSocket dgSocket, String id) throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException, MessageHeaderParsingException, InterruptedException {
		int timeSinceFirstTransmission = 0;
		int timeout = 10000;
		while (true) {
			try {
				// Test 1 including response
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				
				ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);
				
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length);
				send.setPort(3478);
				send.setAddress(InetAddress.getByName("jstun.javawi.de"));
				//dgSocket.connect(stunServer, stunServerPort);
				dgSocket.send(send);
				System.out.println("Test 1: Binding Request sent."+dgSocket.getLocalPort()+"\t"
						+dgSocket.getLocalAddress());
				
				byte[] buf = new byte[2000];
				DatagramPacket receive = new DatagramPacket(buf, buf.length);
				dgSocket.receive(receive);
				MessageHeader receiveMH = new MessageHeader();
				if (!(receiveMH.equalTransactionID(sendMH))) {
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
				}
				
				MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
				ChangedAddress ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
				ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					System.out.println("Message header contains an Errorcode message attribute.");
				}
				if ((ma == null) || (ca == null)) {
					System.out.println("Response does not contain a Mapped Address or Changed Address message attribute.");
				} else {
					System.out.println("Mapped Address: "+ma.getAddress().getInetAddress().getHostName()+" : "+ma.getPort());
					RestUtil.updateNatDetails("https://speech-translator-44168.firebaseio.com/", id, ma.getAddress().getInetAddress().getHostName(), ma.getPort());
					if ((ma.getPort() == dgSocket.getLocalPort()) && (ma.getAddress().getInetAddress().equals(dgSocket.getLocalAddress()))) {
						System.out.println("Node is not natted.");
					} else {
						System.out.println("Node is natted.");
					}
					return ma;
				}
			
			} catch (SocketTimeoutException ste) {
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
			
			return null;
		}
	}
}
