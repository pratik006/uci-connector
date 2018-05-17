package com.prapps.chess.client.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.api.udp.AbstractNetworkBase;

public class ManualTest2 extends AbstractNetworkBase {
	private String id;
	
	public ManualTest2(String id) {
		this.id = id;
	}
	
	public static void main(String[] args) throws Exception {
		InetAddress address = getLocalAddress();
		System.out.println(address);
		DatagramSocket socket1 = new DatagramSocket(new InetSocketAddress(address, 12000));
		updateMacAddress(socket1, "Desky");
		NatDetail otherNat = RestUtil.getOtherNatDetails("lappy");
		
		ManualTest2 test2 = new ManualTest2("Server");
		test2.start(socket1, otherNat);	
	}
	
	public void start(DatagramSocket socket, NatDetail otherNat) {
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					byte[] buf = new byte[2000];
					DatagramPacket p = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(p);
						System.out.println("Recvd socket (port "+socket.getLocalPort()+") "+new String(p.getData()));
					} catch (IOException e) { }
				}
			}
		});t2.start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				String m2 = "hello from "+id;
				try {
					for (int i=0;i<10;i++) {
						byte[] buf = (m2+" from S1 "+otherNat.getPort()+" --- "+socket.getLocalPort()).getBytes();
						DatagramPacket p = new DatagramPacket(buf, buf.length);
						p.setAddress(InetAddress.getByName(otherNat.getHost()));
						p.setPort(otherNat.getPort());
						p.setData(buf);
						socket.send(p);
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket.close();	
				}
			}
		}).start();	
	}

}
