package com.prapps.chess.nat.udp;

import java.net.DatagramPacket;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;

public class P2PMessageListener implements PacketListener {
	private ObjectMapper mapper = new ObjectMapper();
	private long seq;
	
	public P2PMessageListener() {
		
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		Message msg = mapper.readValue(new String(packet.getData()), Message.class);
		//System.out.println(msg);
		if (msg.getType() == Message.ENGINE_TYPE) {
			if (seq+1 == msg.getSeq()) {
				handleMessage(msg);
				seq++;
				for (Message m : queue) {
					if (seq+1 == m.getSeq()) {
						handleMessage(m);
						seq++;
					}
				}
			} else {
				queue.add(msg);
			}	
		} else if (msg.getType() == Message.HANDSHAKE_TYPE) {
			seq = 0;
		} else if (msg.getType() == Message.AVAILABLE_ENGINE_TYPE) {
			send(new Message(1, null, Arrays.toString(engineIds.toArray())));
		}
	}
	
	public void resetSeq() {
		seq = 0;
	}

}
