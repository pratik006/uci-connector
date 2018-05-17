package com.prapps.chess.client.test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.api.udp.AbstractNetworkBase;

public class ManualTest3 extends AbstractNetworkBase {
	
	public static void main(String[] args) throws Exception {
		InetAddress address = getLocalAddress();
		System.out.println(address);
		DatagramSocket socket1 = new DatagramSocket(new InetSocketAddress(address, 12001));
		updateMacAddress(socket1, "lappy");
		NatDetail otherNat = RestUtil.getOtherNatDetails("Desky");
		
		ManualTest2 test2 = new ManualTest2("lappy");
		test2.start(socket1, otherNat);	
	}

}
