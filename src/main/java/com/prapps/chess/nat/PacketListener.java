package com.prapps.chess.nat;

import java.net.DatagramPacket;

public interface PacketListener {
	void onReceive(DatagramPacket packet); 
}
