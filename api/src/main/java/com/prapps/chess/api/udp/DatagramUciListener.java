package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;

public class DatagramUciListener implements PacketListener {
	private Logger LOG = LoggerFactory.getLogger(DatagramUciListener.class);
	private SharedContext ctx;
	private PriorityQueue<Message> queue = new PriorityQueue<>();
	
	public DatagramUciListener(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		if (ctx.getConnectionState().get().getState() == State.HANDSHAKE_TWO_WAY) {
			Message msg;
			try {
				msg = ctx.getObjectMapper().readValue(new String(packet.getData()), Message.class);
				LOG.trace("UCI server msg: "+msg);
				if (msg.getType() != Message.ENGINE_TYPE || System.currentTimeMillis() - msg.getTimestamp() > 30000) {
					LOG.trace("Old packet, discarding");
					return;
				}
				
				StringBuilder sb = new StringBuilder();
				if (ctx.getReadSeq()+1 == msg.getSeq()) {
					sb.append(new String(msg.getData()));
					synchronized (ctx.getReadSeq()) {
						ctx.incrementSeq();	
					}
					
					for (Message m : queue) {
						if (ctx.getReadSeq()+1 == m.getSeq()) {
							sb.append(new String(msg.getData()));
							ctx.incrementSeq();
						}
					}
				} else {
					queue.add(msg);
				}	
				System.out.print(sb.toString());
			} catch(IOException e) { e.printStackTrace(); }
		}
	}

}
