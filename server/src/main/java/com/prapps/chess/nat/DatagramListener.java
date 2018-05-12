package com.prapps.chess.nat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.nat.udp.P2PMessageListener;
import com.prapps.chess.nat.udp.PacketListener;
import com.prapps.chess.nat.udp.StunMessageListener;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class DatagramListener {
	
	private static final String[][] STUN_SERVERS = new String[][] {
			{"jstun.javawi.de","3478"},
			{"stun4.l.google.com","19302"}
	};
	private static final int STUN_SERVER_INDEX = 0;
	
	private String id;
	private DatagramSocket dgSocket;
	
	private InetAddress stunServer;
	private int stunServerPort;
	
	private boolean suspendStun = false;
	private Integer ackCount;
	private static boolean restart = true;
	private static InetAddress srcAddress;
	private static int srcPort = 12000;
	
	private LinkedList<DatagramPacket> packets = new LinkedList<DatagramPacket>();
	private List<PacketListener> packetListeners = new ArrayList<PacketListener>();
	private P2PMessageListener p2pMessageListener;
	private StunMessageListener stunMessageListener;
	private static NatDetail otherNat;
	
	public DatagramListener(String id, DatagramSocket dgSocket, InetAddress stunServer, int stunServerPort) {
		this.id = id;
		this.dgSocket = dgSocket;
		this.stunServer = stunServer;
		this.stunServerPort = stunServerPort;
		this.ackCount = 0;
		p2pMessageListener = new P2PMessageListener(ackCount, true);
		stunMessageListener = new StunMessageListener(this.id);
		packetListeners.add(p2pMessageListener);
		packetListeners.add(stunMessageListener);
	}

	public static void main(String[] args) throws Exception {
		srcAddress = getLocalAddress();
		DatagramSocket dgSocket = new DatagramSocket(new InetSocketAddress(srcAddress, srcPort));
		dgSocket.setSoTimeout(10000);
		dgSocket.setReuseAddress(true);
		InetAddress stunServer = InetAddress.getByName(STUN_SERVERS[STUN_SERVER_INDEX][0]);
		int stunServerPort = Integer.parseInt(STUN_SERVERS[STUN_SERVER_INDEX][1]);
		dgSocket.connect(stunServer, stunServerPort);
		DatagramListener listener = new DatagramListener("Desky", dgSocket, stunServer, stunServerPort);
		listener.getMappedAddress(dgSocket);
		otherNat = RestUtil.getOtherNatDetails("lappy");
		listener.initListeners();
		while ((new Date().getTime() - otherNat.getLastUpdated()) > 1000*60*5) {
			Thread.sleep(5000);
			otherNat = RestUtil.getOtherNatDetails("lappy");
		}
		System.out.println("Other client is online");
		new Thread(new Runnable() {
			@Override
			public void run() {
				otherNat = RestUtil.getOtherNatDetails("lappy");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		//while (restart) {
			listener.listen();
			//if (restart) {
				//System.out.println("restarting");
			//}
		//}
	}
	
	public void initListeners() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					DatagramPacket receive = new DatagramPacket(new byte[2000], 2000);
					try {
						System.out.println("waiting for msg");
						dgSocket.receive(receive);
						synchronized (packets) {
							packets.add(receive);	
							packets.notifyAll();
						}
						packetListeners.forEach((listener) -> {
							DatagramPacket pkt = new DatagramPacket(receive.getData(), receive.getData().length);
							listener.onReceive(pkt);
						});
						
						//System.out.println("received data: "+new String(receive.getData()));
					} catch(java.net.SocketTimeoutException ex) {
						System.out.println("socket timeout");
						//do nothing
					} catch(java.net.PortUnreachableException ex) {
						ex.printStackTrace();
						restart = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}		
			}
		}).start();
	}
	
	public void listen() throws IOException, InterruptedException {
		restart = false;
		new Thread(new Runnable() {
			public void run() {
				String msg = "ack";
				try {
					InetAddress targetAddress = InetAddress.getByName(otherNat.getHost());
					dgSocket.connect(targetAddress, otherNat.getPort());
					while (ackCount < 100 && !restart) {
						suspendStun = true;
						DatagramPacket send = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
						dgSocket.send(send);
						//System.out.println(id+" sent ack"+send.getAddress().getHostName()+":"+send.getPort());
						Thread.sleep(200);
					}
					
					/*EngineClientAdapter adapter = new EngineClientAdapter(InetAddress.getByName(nat.getHost()), nat.getPort());
					adapter.listen();*/
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		
	}
	
	public void registerListener(PacketListener listener) {
		packetListeners.add(listener);
	}
	
	public void removeListener(PacketListener listener) {
		packetListeners.remove(listener);
	}
	
	private String getMappedAddress(DatagramSocket dgSocket) throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException, MessageHeaderParsingException, InterruptedException {
		int timeSinceFirstTransmission = 0;
		int timeout = 10000;
		while (true) {
			if(suspendStun) {
				Thread.sleep(1000);
				continue;
			}
			
			try {
				// Test 1 including response
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				
				ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);
				
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length);
				stunMessageListener.setSendMH(sendMH);
				dgSocket.connect(stunServer, stunServerPort);
				dgSocket.send(send);
				System.out.println("Test 1: Binding Request sent.");
			
			} catch (SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < 7900) {
					System.out.println("Test 1: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = (timeSinceFirstTransmission * 2);
					if (timeoutAddValue > 1600) timeoutAddValue = 1600;
					timeout = timeoutAddValue;
				} else {
					// node is not capable of udp communication
					System.out.println("Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					System.out.println("Node is not capable of UDP communication.");
					
				}
			}
			
			return null;
		}
	}
	
	public static InetAddress getLocalAddress() throws ClassNotFoundException, SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
			while (iaddresses.hasMoreElements() && iface.getDisplayName().indexOf("docker") == -1) {
				InetAddress iaddress = iaddresses.nextElement();
				if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
					if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
						return iaddress;
					}
				}
			}
		}
		
		return null;
	}

}
