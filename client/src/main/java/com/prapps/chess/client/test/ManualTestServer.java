package com.prapps.chess.client.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.api.udp.AbstractNetworkBase;

public class ManualTestServer extends AbstractNetworkBase {
	private String id;
	private boolean exit;
	private ServerSocket serverSocket;
	
	public ManualTestServer(String id) {
		this.id = id;
	}
	
	public static void main(String[] args) throws Exception {
		InetAddress address = getLocalAddress();
		DatagramSocket socket1 = new DatagramSocket(new InetSocketAddress(address, 12000));
		socket1.setReuseAddress(true);
		updateMacAddress(socket1, "Desky");
		NatDetail otherNat = RestUtil.getOtherNatDetails("lappy");
		System.out.println(otherNat);
		ManualTestServer test = new ManualTestServer("Desky");
		test.start(socket1, otherNat);	
	}
	
	public void start(DatagramSocket socket, NatDetail otherNat) {
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				//while (!exit) {
					byte[] buf = new byte[2000];
					DatagramPacket p = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(p);
						System.out.println("Recvd socket (port "+socket.getLocalPort()+") "+new String(p.getData()));
					} catch (IOException e) { }
				//}
				socket.close();
				try {
					if (serverSocket == null) {
						serverSocket = new ServerSocket(12000);
						System.out.println("sopcket created");
						Socket clientSocket = serverSocket.accept();
						System.out.println("connection established\n\n\n");
						clientSocket.close();
					}
				} catch(Exception e) {e.printStackTrace();}
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
				}
			}
		}).start();	
	}

}
