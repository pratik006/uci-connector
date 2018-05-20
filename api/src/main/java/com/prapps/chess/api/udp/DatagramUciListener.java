package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prapps.chess.api.Message;

public class DatagramUciListener implements PacketListener {
	private Logger LOG = LoggerFactory.getLogger(DatagramUciListener.class);
	private SharedContext ctx;
	private PriorityQueue<Message> queue = new PriorityQueue<>();
	private AtomicLong seq = new AtomicLong(0);
	
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
				if (System.currentTimeMillis() - msg.getTimestamp() > 30000) {
					LOG.trace("Old packet, discarding");
					return;
				}
				
				if (msg.getType() == Message.ENGINE_TYPE) {
					if (seq.get()+1 == msg.getSeq()) {
						System.out.print(new String(msg.getData()));
						synchronized (seq) {
							seq.incrementAndGet();	
						}
						for (Message m : queue) {
							if (seq.get()+1 == m.getSeq()) {
								System.out.print(new String(msg.getData()));
								seq.incrementAndGet();
							}
						}
					} else {
						queue.add(msg);
					}	
				} 
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}
