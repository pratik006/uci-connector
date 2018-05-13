package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;

public abstract class AbstractP2PListener implements PacketListener {
	private Logger LOG = LoggerFactory.getLogger(AbstractP2PListener.class);
	protected long seq;
	protected SharedContext ctx;
	
	public AbstractP2PListener(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		Message msg;
		try {
			msg = ctx.getObjectMapper().readValue(new String(packet.getData()), Message.class);
			LOG.debug("P2P client: "+msg);
			if (System.currentTimeMillis() - msg.getTimestamp() > 10000) {
				LOG.debug("Old packet, discarding");
				return;
			}
			
			if (msg.getType() == Message.HANDSHAKE_TYPE) {
				seq = 0;
				if (ctx.getConnectionState().get().getState() ==  State.MAC_UPDATED 
						&& ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_ONE_WAY)) {
					synchronized (ctx.getConnectionState()) {
						ctx.getConnectionState().get().setState(State.HANDSHAKE_ONE_WAY);
						ctx.getConnectionState().notifyAll();	
					}	
				}
				ctx.send(Message.HANDSHAKE_COMNPLETE_TYPE);
			} else if (msg.getType() == Message.HANDSHAKE_COMNPLETE_TYPE) {
				if (ctx.getConnectionState().get().getState() ==  State.HANDSHAKE_ONE_WAY 
						&& ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_TWO_WAY)) {
					synchronized (ctx.getConnectionState()) {
						ctx.getConnectionState().get().setState(State.HANDSHAKE_TWO_WAY);
						ctx.getConnectionState().notifyAll();	
					}	
				}
			}
		} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
