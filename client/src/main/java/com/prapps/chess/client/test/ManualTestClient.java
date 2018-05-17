package com.prapps.chess.client.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.api.udp.AbstractNetworkBase;

public class ManualTestClient extends AbstractNetworkBase {
	private String id;
	private boolean exit;
	
	public ManualTestClient(String id) {
		this.id = id;
	}
	
	public static void main(String[] args) throws Exception {
		InetAddress address = getLocalAddress();
		DatagramSocket socket2 = new DatagramSocket(new InetSocketAddress(address, 12001));
		updateMacAddress(socket2, "lappy");
		NatDetail serverNat = RestUtil.getOtherNatDetails("Desky");
		
		System.out.println(serverNat);
		
		ManualTestClient test1 = new ManualTestClient("lappy");
		test1.start(socket2, serverNat);
	}
	
	public void start(DatagramSocket socket, NatDetail otherNat) {
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!exit) {
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
					exit = true;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket.close();	
				}
			}
		}).start();	
	}

}
