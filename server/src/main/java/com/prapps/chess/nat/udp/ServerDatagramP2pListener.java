package com.prapps.chess.nat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.udp.PacketListener;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.State;
import com.prapps.chess.nat.AbstractNetworkListener;
import com.prapps.chess.server.config.ServerConfig;

public class ServerDatagramP2pListener extends AbstractNetworkListener implements PacketListener {
	private Logger LOG = LoggerFactory.getLogger(ServerDatagramP2pListener.class);
	private AtomicLong seq;
	private PriorityQueue<Message> queue = new PriorityQueue<>();
	
	
	public ServerDatagramP2pListener(SharedContext ctx, ServerConfig serverConfig, AtomicLong seq) {
		super(ctx, serverConfig);
		this.seq = seq;
	}

	@Override
	public void run() {
		
	}

	@Override
	protected void send(Message msg) {
		
	}

	@Override
	public void onReceive(DatagramPacket packet) {
		Message msg;
		try {
			msg = mapper.readValue(new String(packet.getData()), Message.class);
			LOG.debug("P2P server msg: "+msg);
			if (System.currentTimeMillis() - msg.getTimestamp() > 30000) {
				LOG.debug("Old packet, discarding");
				return;
			}
			
			if (msg.getType() == Message.ENGINE_TYPE) {
				if (seq.get()+1 == msg.getSeq()) {
					handleMessage(msg);
					seq.incrementAndGet();
					for (Message m : queue) {
						if (seq.get()+1 == m.getSeq()) {
							handleMessage(m);
							seq.incrementAndGet();
						}
					}
				} else {
					queue.add(msg);
				}	
			} else if (msg.getType() == Message.HANDSHAKE_TYPE) {
				seq.set(0);
				if (ctx.getConnectionState().get().getState() == State.MAC_UPDATED 
						&& ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_ONE_WAY)) {
					synchronized (ctx.getConnectionState()) {
						ctx.getConnectionState().get().setState(State.HANDSHAKE_ONE_WAY);
						ctx.getConnectionState().notifyAll();
					}
					ctx.send(Message.HANDSHAKE_COMNPLETE_TYPE);
				}
			} else if (msg.getType() == Message.HANDSHAKE_COMNPLETE_TYPE) {
				if (ctx.getConnectionState().get().getState() == State.HANDSHAKE_ONE_WAY 
						&& ctx.getConnectionState().get().isHigherState(State.HANDSHAKE_TWO_WAY)) {
					synchronized (ctx.getConnectionState()) {
						ctx.getConnectionState().get().setState(State.HANDSHAKE_TWO_WAY);
						ctx.getConnectionState().notifyAll();	
					}	
				}
			} else if (msg.getType() == Message.AVAILABLE_ENGINE_TYPE) {
				send(new Message(1, null, Arrays.toString(engineIds.toArray())));
			}
		} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
