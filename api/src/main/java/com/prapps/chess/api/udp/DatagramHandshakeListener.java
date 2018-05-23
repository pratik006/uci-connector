package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;

public class DatagramHandshakeListener implements PacketListener {
	private Logger LOG = LoggerFactory.getLogger(DatagramHandshakeListener.class);
	
	private SharedContext ctx;
	
	public DatagramHandshakeListener(SharedContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void onReceive(DatagramPacket packet) {
		Message msg;
		try {
			msg = ctx.getObjectMapper().readValue(new String(packet.getData()), Message.class);
			if (System.currentTimeMillis() - msg.getTimestamp() > 300000) {
				LOG.trace("Old packet, discarding.. current time "+System.currentTimeMillis()+" Packet Time: "+msg.getTimestamp());
				return;
			}
			
			LOG.trace("P2P msg: "+msg+" data: "+new String(msg.getData()));
			if (msg.isAck()) {
				if (msg.getType() == ctx.getState() || !ctx.isSameUuid(msg)) {
					LOG.debug("Discard packet ");
					return;
				}
				
				LOG.debug("uuid matched "+new String(msg.getData())+" "+msg.getType());
				if (msg.getType() == State.RESET_CONNECTION) {
					if (ctx.getState() != State.RESET_CONNECTION) {
						if (ctx.getState() >= State.RECEIVED_OTHER_MAC) {
							synchronized (ctx.getConnectionState()) {
								ctx.getConnectionState().get().setState(State.RESET_CONNECTION);
								ctx.getConnectionState().notifyAll();
							}
						}
					}	
				} else {
					if (ctx.getState() <= msg.getType()-1) {
						if (ctx.getConnectionState().get().isHigherState(msg.getType())) {
							synchronized (ctx.getConnectionState()) {
								ctx.getConnectionState().get().setState(msg.getType());
								ctx.getConnectionState().notifyAll();
							}
						}
					}
				}
				
			} else if (msg.getType() >= 100) {
				//engine type
			} else {
				Message newMsg = new Message(msg.getType());
				newMsg.setData(msg.getData());
				newMsg.setAck(Boolean.TRUE);
				newMsg.setType(msg.getType());
				if (msg.getType() == State.RESET_CONNECTION) {
					if (ctx.getNat().get() == null || ctx.getNat().get().getHost() == null) {
						NatDetail nat = new NatDetail();
						nat.setHost(msg.getHost());
						nat.setPort(msg.getPort());
						ctx.getNat().set(nat);
					}
					if (ctx.getState() != State.RESET_CONNECTION) {
						ctx.send(State.RESET_CONNECTION);
					}
				}
				ctx.send(newMsg);
			}
		} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
