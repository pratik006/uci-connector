package com.prapps.chess.client.test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.api.udp.AbstractNetworkBase;

public class ManualTestServer extends AbstractNetworkBase {

	public static void main(String[] args) throws Exception {
		InetAddress address = getLocalAddress();
		DatagramSocket socket1 = new DatagramSocket(new InetSocketAddress(address, 12001));
		updateMacAddress(socket1, "Desky");
		NatDetail otherNat = RestUtil.getOtherNatDetails("lappy");
		
		ManualTestClient test = new ManualTestClient("Desky");
		test.start(socket1, otherNat);	
	}

}
