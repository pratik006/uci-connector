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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class DatagramListener {
	private String id;
	private DatagramSocket dgSocket;
	
	private InetAddress stunServer;
	private int stunServerPort;
	
	private boolean suspendStun = false;
	
	private LinkedList<DatagramPacket> packets = new LinkedList<DatagramPacket>();
	private List<PacketListener> packetListeners = new ArrayList<PacketListener>();
	private P2PMessageListener p2pMessageListener = new P2PMessageListener();
	private StunMessageListener stunMessageListener;
	
	public DatagramListener(String id, DatagramSocket dgSocket, InetAddress stunServer, int stunServerPort) {
		this.id = id;
		this.dgSocket = dgSocket;
		this.stunServer = stunServer;
		this.stunServerPort = stunServerPort;
		stunMessageListener = new StunMessageListener(this.id);
		packetListeners.add(p2pMessageListener);
		packetListeners.add(stunMessageListener);
	}

	public static void main(String[] args) throws Exception {
		InetAddress iaddress = getLocalAddress();
		DatagramSocket dgSocket = new DatagramSocket(new InetSocketAddress(iaddress, 12000));
		dgSocket.setSoTimeout(10000);
		dgSocket.setReuseAddress(true);
		dgSocket.connect(InetAddress.getByName("jstun.javawi.de"), 3478);
		DatagramListener listener = new DatagramListener("Desky", dgSocket, InetAddress.getByName("jstun.javawi.de"), 3478);
		listener.listen();
	}
	
	public void listen() throws IOException {
		final DatagramListener me = this;
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						me.getMappedAddress(dgSocket);
						Thread.sleep(10000);
					}
				} catch (Exception e) { e.printStackTrace(); } 
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					DatagramPacket receive = new DatagramPacket(new byte[2000], 2000);
					try {
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
						//do nothing
					} catch (IOException e) {
						e.printStackTrace();
					}
				}		
			}
		}).start();
		
		/*new Thread(new Runnable() {
			public void run() {
				NatDetail nat = null;
				while (nat == null) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					nat = RestUtil.getOtherNatDetails("lappy");
				}
				
				System.out.println(nat+"\tsending msg hello");
				String msg = "hello there ! Lappy";
				while (true) {
					try {
						DatagramPacket send = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(nat.getHost()), nat.getPort());
						suspendStun = true;
						dgSocket.connect(InetAddress.getByName(nat.getHost()), nat.getPort());
						dgSocket.send(send);
						Thread.sleep(1000);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();*/
		
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
