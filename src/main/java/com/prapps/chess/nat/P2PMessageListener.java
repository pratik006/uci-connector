package com.prapps.chess.nat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PMessageListener implements PacketListener {
	private Integer ackCount;
	
	public P2PMessageListener(Integer ackCount) {
		this.ackCount = ackCount;
	}
	
	@Override
	public void onReceive(DatagramPacket packet) {
		System.out.println("Recvd: "+new String(packet.getData())+" "+ackCount);
		if (new String(packet.getData()).indexOf("ack") != -1) {
			ackCount++;
		}
		
		if (ackCount > 100) {
			ackCount = 0;
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(12000);
				while(true) {
					System.out.println("waiting for conn..");
					Socket socket = serverSocket.accept();
					byte[] buf = new byte[2048];
					while (socket.getInputStream().read(buf) != -1) {
						System.out.println("Incoming msg: "+new String(buf));
					}
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
