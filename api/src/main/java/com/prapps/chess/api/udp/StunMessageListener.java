package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.atomic.AtomicReference;

import com.prapps.chess.api.RestUtil;

import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class StunMessageListener implements PacketListener {
	private AtomicReference<MessageHeader> sendMHRef;
	private String externalHost;
	private String id;
	
	public StunMessageListener(AtomicReference<MessageHeader> sendMHRef, String externalHost, String id) {
		this.sendMHRef = sendMHRef;
		this.externalHost = externalHost;
		this.id = id;
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		MessageHeader receiveMH = new MessageHeader();
		if (!(receiveMH.equalTransactionID(sendMHRef.get()))) {
			try {
				receiveMH = MessageHeader.parseHeader(packet.getData());
				receiveMH.parseAttributes(packet.getData());
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
					
					if (ma != null)
						RestUtil.updateNatDetails(externalHost, id, ma.getAddress().getInetAddress().getHostName(), ma.getPort());
				}
			} catch (MessageHeaderParsingException e) {
				System.out.println("Cannot parse: "+new String(packet.getData()));
			}
			catch (MessageAttributeParsingException | UtilityException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
