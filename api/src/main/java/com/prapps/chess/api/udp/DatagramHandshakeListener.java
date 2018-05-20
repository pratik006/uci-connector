package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;

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
			LOG.trace("P2P msg: "+msg);
			if (System.currentTimeMillis() - msg.getTimestamp() > 30000) {
				LOG.trace("Old packet, discarding");
				return;
			}
			
			if (msg.getType() == Message.HANDSHAKE_TYPE) {
				if (ctx.isSameUuid(msg) && ctx.getConnectionState().get().getState() <= State.RECEIVED_OTHER_MAC) {
					LOG.debug("uuid matched "+new String(msg.getData()));
					if (ctx.getConnectionState().get().getState() >= State.MAC_UPDATED) {
						if (ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_ONE_WAY)) {
							synchronized (ctx.getConnectionState()) {
								ctx.getConnectionState().get().setState(State.HANDSHAKE_ONE_WAY);
								ctx.getConnectionState().notifyAll();
							}
							ctx.resetUuid();
							Message newMsg = new Message(Message.HANDSHAKE_COMNPLETE_TYPE);
							newMsg.setData(msg.getData());
							ctx.send(newMsg);
						}
					}
				} else {
					Message newMsg = new Message(Message.HANDSHAKE_TYPE);
					newMsg.setData(msg.getData());
					newMsg.setPort(msg.getPort());
					newMsg.setHost(msg.getHost());
					ctx.send(newMsg);
				}
			} else if (msg.getType() == Message.HANDSHAKE_COMNPLETE_TYPE) {
				if (ctx.isSameUuid(msg) && ctx.getConnectionState().get().getState() <= State.HANDSHAKE_ONE_WAY) {
					LOG.trace("uuid matched "+new String(msg.getData()));
					if (ctx.getConnectionState().get().getState() == State.HANDSHAKE_ONE_WAY 
							&& ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_TWO_WAY)) {
						synchronized (ctx.getConnectionState()) {
							ctx.getConnectionState().get().setState(State.HANDSHAKE_TWO_WAY);
							ctx.getConnectionState().notifyAll();	
						}	
						ctx.resetUuid();
						ctx.resetSeq();
					}	
				} else {
					Message newMsg = new Message(Message.HANDSHAKE_COMNPLETE_TYPE);
					newMsg.setData(msg.getData());
					ctx.send(newMsg);
				}
			}
		} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
