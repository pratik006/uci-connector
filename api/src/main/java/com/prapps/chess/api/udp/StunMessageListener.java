package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;

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
	private SharedContext ctx;
	
	public StunMessageListener(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		MessageHeader receiveMH = new MessageHeader();
		if (!(receiveMH.equalTransactionID(ctx.getSendMHRef().get()))) {
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
					//System.out.println("Mapped address "+ma.getAddress().getInetAddress().getHostName()+" : "+ma.getPort());
					
					if (ma != null) {
						RestUtil.updateNatDetails(ctx.getBaseConfig().getExternalHost(), ctx.getId(), 
								ma.getAddress().getInetAddress().getHostName(), ma.getPort());
						if (ctx.getConnectionState().get().isHigherState(State.MAC_UPDATED)) {
							synchronized (ctx.getConnectionState()) {
								ctx.getConnectionState().get().setState(State.MAC_UPDATED);
								ctx.getConnectionState().notifyAll();
							}	
						}
						
						try { Thread.sleep(SharedContext.TIME_DIFF_ALLOWED); } catch (InterruptedException e) { e.printStackTrace(); }
					}
				}
			} catch (MessageHeaderParsingException e) {
				//System.out.println("Cannot parse: "+new String(packet.getData()));
			}
			catch (MessageAttributeParsingException | UtilityException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
