package com.prapps.chess.api.udp;

import java.net.DatagramPacket;

public interface PacketListener {
	void onReceive(DatagramPacket packet); 
}
