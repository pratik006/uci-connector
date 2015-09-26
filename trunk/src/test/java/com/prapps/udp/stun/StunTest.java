package com.prapps.udp.stun;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class StunTest {

	public static String stun1 = "stun.faktortel.com.au";
	public static int port1=3478;
	
	public static String stun2 = "stun1.voiceeclipse.net";
	public static int port2=3478;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int port = port1;
		InetAddress addressToMap = InetAddress.getByName(stun1);
		
		DatagramSocket datagramSocket = new DatagramSocket();
		byte[] buf = new byte[StunHeader.STUN_HEADER_LENGTH
		                      + StunHeader.TLV_LENGTH + StunHeader.MAPPED_ADDRESS_LENGTH];

		                  buf[1] = (byte) StunHeader.BINDING_REQUEST;
		                  buf[3] = (byte) StunHeader.TLV_LENGTH + 
		                      StunHeader.MAPPED_ADDRESS_LENGTH;
		                  
		                  long time = System.currentTimeMillis();

		                  for (int i = 0; i < 16; i++) {
		                      buf[i + 4] = (byte) (time >> ((i % 4) * 8));
		                  }

		                  buf[21] = StunHeader.MAPPED_ADDRESS;
		                  buf[23] = StunHeader.MAPPED_ADDRESS_LENGTH;

		                  buf[25] = 1;    // address family

		                  buf[26] = (byte) (port >> 8);
		                  buf[27] = (byte) (port & 0xff);

		                  byte[] address = addressToMap.getAddress();

		                  buf[28] = address[0];   // address to map
		                  buf[29] = address[1];
		                  buf[30] = address[2];
		                  buf[31] = address[3];
		DatagramPacket newPacket = new DatagramPacket(buf,buf.length, addressToMap, port);
		datagramSocket.send(newPacket);
		System.out.println("datagram sent");
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		datagramSocket.receive(packet);
		byte[] response = packet.getData();
		System.out.println("recvd"+packet.getAddress()+":"+packet.getPort());
		 int type = (int) 
	                ((response[0] << 8 & 0xff00) | (response[1] & 0xff));

	            if (type == StunHeader.BINDING_RESPONSE) {
	                InetSocketAddress mappedAddress = StunHeader.getAddress(response, StunHeader.MAPPED_ADDRESS);
	                System.out.println(mappedAddress);
	            }

		//System.out.println(new String(packet.getData(), 0,packet.getLength()));
	}

}
