package com.prapps.chess.nat;

import java.io.IOException;
import java.net.DatagramPacket;

import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class StunMessageListener implements PacketListener {

	private String id;
	private MessageHeader sendMH; 
	
	public StunMessageListener(String id) {
		this.id = id;
	}
	
	@Override
	public void onReceive(DatagramPacket receive) {
		if (sendMH == null) {
			return;
		}
		
		try {
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
				System.out.println("Mapped address "+ma.getAddress().getInetAddress().getHostName()+" : "+ma.getPort());
				RestUtil.updateNatDetails(id, ma.getAddress().getInetAddress().getHostName(), ma.getPort());
				/*if ((ma.getPort() == dgSocket.getLocalPort()) && (ma.getAddress().getInetAddress().equals(dgSocket.getLocalAddress()))) {
					System.out.println("Node is not natted.");
				} else {
					System.out.println("Node is natted.");
				}*/
			}
		} catch(IOException | UtilityException | MessageAttributeParsingException ex) {
			ex.printStackTrace();
		} catch (MessageHeaderParsingException e) {
			//
		}
		
	}

	public MessageHeader getSendMH() {
		return sendMH;
	}

	public void setSendMH(MessageHeader sendMH) {
		this.sendMH = sendMH;
	}

}
