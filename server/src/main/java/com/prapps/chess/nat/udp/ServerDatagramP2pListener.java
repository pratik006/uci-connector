package com.prapps.chess.nat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
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
	private PriorityQueue<Message> queue = new PriorityQueue<>();
	
	
	public ServerDatagramP2pListener(SharedContext ctx, ServerConfig serverConfig, AtomicLong seq) {
		super(ctx, serverConfig);
	}

	@Override
	public void run() {
		
	}

	@Override
	protected void send(Message msg) {
		try {
			LOG.debug("length: "+msg.getData().length);
			ctx.send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReceive(DatagramPacket packet) {
		if (ctx.getConnectionState().get().getState() == State.HANDSHAKE_TWO_WAY) {
			Message msg;
			try {
				msg = ctx.getObjectMapper().readValue(new String(packet.getData()), Message.class);
				LOG.trace("UCI server msg: "+msg);
				if (System.currentTimeMillis() - msg.getTimestamp() > 30000) {
					LOG.trace("Old packet, discarding");
					return;
				}
				
				LOG.trace("Expecting seq "+(ctx.getReadSeq()+1)+" has "+msg.getSeq());
				if (ctx.getReadSeq() > msg.getSeq()) {
					LOG.error("discarding... incorrect sequence");
					return;
				}
				if (ctx.getReadSeq()+1 == msg.getSeq()) {
					handleMessage(msg);
					synchronized (ctx.getReadSeq()) {
						ctx.incrementSeq();	
					}
					
					for (Message m : queue) {
						if (ctx.getReadSeq()+1 == m.getSeq()) {
							handleMessage(msg);
							ctx.incrementSeq();
						}
					}
				} else {
					queue.add(msg);
				}	
			} catch (IOException e) { }
		}
	}
	
}
