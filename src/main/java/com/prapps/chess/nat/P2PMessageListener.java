package com.prapps.chess.nat;

import java.net.DatagramPacket;

public class P2PMessageListener implements PacketListener {
	@Override
	public void onReceive(DatagramPacket packet) {
		System.out.println("Recvd: "+new String(packet.getData()));
	}

}
