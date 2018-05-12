package com.prapps.chess.nat.udp;

import java.net.DatagramPacket;

public interface PacketListener {
	void onReceive(DatagramPacket packet); 
}
