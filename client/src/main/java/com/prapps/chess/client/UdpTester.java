package com.prapps.chess.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.ClientConfig;
import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.client.config.ConfigLoader;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class UdpTester {

	public static void main(String[] args) throws SocketException {
		AtomicBoolean exit = new AtomicBoolean(false);
		DatagramSocket socket = new DatagramSocket(13000);
		socket.setReuseAddress(true);
		socket.setSoTimeout(5000);
		ObjectMapper mapper = new ObjectMapper();
		ClientConfig clientConfig = ConfigLoader.INSTANCE.getServerConfig();
		AtomicReference<MessageHeader> sendMHRef = new AtomicReference<MessageHeader>();

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!exit.get()) {
					byte[] buf = new byte[1024 * 1024];
					DatagramPacket p = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(p);
						Message m = mapper.readValue(new String(p.getData()), Message.class);
						System.out.println(new String(m.getData()));
					} catch(com.fasterxml.jackson.core.JsonParseException e) {
						MessageHeader receiveMH = new MessageHeader();
						if (!(receiveMH.equalTransactionID(sendMHRef.get()))) {
							try {
								receiveMH = MessageHeader.parseHeader(p.getData());
								receiveMH.parseAttributes(p.getData());
								MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
								ChangedAddress ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
								ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
								if (ec != null) {
									System.out.println("Message header contains an Errorcode message attribute.");
								}
								if ((ma == null) || (ca == null)) {
									System.out.println("Response does not contain a Mapped Address or Changed Address message attribute.");
								} else {
									System.out.println("Mapped address "+ma.getAddress().getInetAddress().getHostName()+" : "+ma.getPort());
									if (ma != null)
										RestUtil.updateNatDetails(clientConfig.getExternalHost(), "lappy", ma.getAddress().getInetAddress().getHostName(), ma.getPort());
								}
							} catch (MessageHeaderParsingException | MessageAttributeParsingException | UnknownHostException | UtilityException e1) {
								e1.printStackTrace();
							}
						}
					}
					catch (java.net.SocketTimeoutException e) {/*dont o anything*/ } 
					  catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("exit t2");
			}
		});
		t2.start();
		
		byte[] buf;
		try {
			//InetAddress address = AbstractNetworkListener.getLocalAddress();
			int timeSinceFirstTransmission = 0;
			int timeout = 10000;
			while (!exit.get()) {
				try {
					// Test 1 including response
					MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
					sendMH.generateTransactionID();
					sendMHRef.set(sendMH);
					
					ChangeRequest changeRequest = new ChangeRequest();
					sendMH.addMessageAttribute(changeRequest);
					
					byte[] data = sendMH.getBytes();
					InetAddress stunServer = InetAddress.getByName(clientConfig.getUdpConfig().getStunServers().get(0).getHost());
					DatagramPacket send = new DatagramPacket(data, data.length, stunServer, 3478);
					socket.connect(stunServer, 3478);
					socket.send(send);
					
					buf = new byte[2000];
					DatagramPacket receive = new DatagramPacket(buf, buf.length);
					socket.receive(receive);
				} catch (UtilityException | IOException ste) {
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
				try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			
			buf = (mapper.writeValueAsString(new Message(Message.HANDSHAKE_TYPE)) + "\n").getBytes();
			NatDetail nat = RestUtil.getOtherNatDetails(clientConfig.getExternalHost(), "lappy");
			/*InetAddress address = AbstractNetworkListener.getLocalAddress();
			System.out.println(address.getHostName());*/
			DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName(nat.getHost()),nat.getPort());
			socket.send(p);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				String line = null;
				int seq = 1;
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					while (!exit.get()) {
						while (!exit.get() && (line = reader.readLine()) != null) {
							System.out.println(line);
							byte[] buf = (mapper.writeValueAsString(new Message(seq++, "critter", line + "\n")) + "\n")
									.getBytes();
							DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"),
									12312);
							socket.send(p);
							if ("quit".equalsIgnoreCase(line))
								exit.set(true);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("exit t1");
			}
		});
		t1.start();

		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}

}
