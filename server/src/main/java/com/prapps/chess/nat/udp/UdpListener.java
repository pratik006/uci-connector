package com.prapps.chess.nat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.spi.ErrorCode;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.nat.AbstractNetworkListener;
import com.prapps.chess.server.config.ServerConfig;

import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class UdpListener extends AbstractNetworkListener implements Runnable {
	private DatagramSocket dgSocket;
	
	private static InetAddress srcAddress;
	private int srcPort;
	private InetAddress targetAddress;
	private int targetPort;
	
	private static NatDetail otherNat;
	private Map<String, Long> sequenceMap = new HashMap<>();
	
	private List<PacketListener> listeners;
	private AtomicReference<MessageHeader> sendMH = new AtomicReference<MessageHeader>();
	
	private long seq;
	private PriorityQueue<Message> queue = new PriorityQueue<>();
	private boolean connected = false;
	
	public UdpListener(AtomicBoolean exit, ServerConfig serverConfig) throws ClassNotFoundException, SocketException, UnknownHostException {
		super(exit, serverConfig);
		this.srcPort = serverConfig.getUdpConfig().getSourcePort();
		//srcAddress = InetAddress.getByName("localhost");
		srcAddress = getLocalAddress();
		listeners = new ArrayList<>();
		listeners.add(new PacketListener() {
			@Override
			public void onReceive(DatagramPacket receive) {
				try {
					MessageHeader receiveMH = new MessageHeader();
					if (!(receiveMH.equalTransactionID(sendMH.get()))) {
						receiveMH = MessageHeader.parseHeader(receive.getData());
						receiveMH.parseAttributes(receive.getData());
					}
					
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
							RestUtil.updateNatDetails(serverConfig.getExternalHost(), "Desky", ma.getAddress().getInetAddress().getHostName(), ma.getPort());
						/*if ((ma.getPort() == dgSocket.getLocalPort()) && (ma.getAddress().getInetAddress().equals(dgSocket.getLocalAddress()))) {
							System.out.println("Node is not natted.");
						} else {
							System.out.println("Node is natted.");
						}*/
					}
				} catch(IOException | UtilityException | MessageAttributeParsingException ex) {
					ex.printStackTrace();
				} catch (MessageHeaderParsingException e) {
					e.printStackTrace();
				}				
			}
		});
		listeners.add(new PacketListener() {
			@Override
			public void onReceive(DatagramPacket packet) {
				Message msg;
				try {
					msg = mapper.readValue(new String(packet.getData()), Message.class);
					//System.out.println(msg);
					if (msg.getType() == Message.ENGINE_TYPE) {
						if (seq+1 == msg.getSeq()) {
							handleMessage(msg);
							seq++;
							for (Message m : queue) {
								if (seq+1 == m.getSeq()) {
									handleMessage(m);
									seq++;
								}
							}
						} else {
							queue.add(msg);
						}	
					} else if (msg.getType() == Message.HANDSHAKE_TYPE) {
						seq = 0;
						connected = true;
					} else if (msg.getType() == Message.AVAILABLE_ENGINE_TYPE) {
						send(new Message(1, null, Arrays.toString(engineIds.toArray())));
					}
				} catch(com.fasterxml.jackson.core.JsonParseException e) {} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void init() throws SocketException, UnknownHostException {
		dgSocket = new DatagramSocket(new InetSocketAddress(srcAddress, srcPort));
		//System.out.println(srcAddress+"\t"+srcPort);
		dgSocket.setSoTimeout(10000);
		dgSocket.setReuseAddress(true);
		new Thread(new MacAddressUpdaterThread(exit, dgSocket, serverConfig, sendMH)).start();
	}

	@Override
	public void run() {
		try {
			init();
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(!exit.get()) {
						NatDetail nat = RestUtil.getOtherNatDetails(serverConfig.getExternalHost(), "lappy");
						Calendar cal = Calendar.getInstance();
						if (cal.getTimeInMillis() - nat.getLastUpdated() > 360000) {
							//System.out.println("Client not online...");
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							while (!connected) {
								try {
									targetAddress = InetAddress.getByName(nat.getHost());
								} catch (UnknownHostException e1) {
									e1.printStackTrace();
								}
								targetPort = nat.getPort();
								send(new Message(Message.HANDSHAKE_TYPE));
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}).start();
			while (!exit.get()) {
				DatagramPacket p = new DatagramPacket(new byte[2000], 2000);
				try {
					dgSocket.receive(p);
					targetAddress = p.getAddress();
					targetPort = p.getPort();
					for (PacketListener listener : listeners) {
						listener.onReceive(p);
					}
				} catch (java.net.SocketTimeoutException e1) { }
			}
		} catch(com.fasterxml.jackson.core.JsonParseException e1) { }
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	@Override
	protected void send(Message msg) {
		if (!sequenceMap.containsKey(targetAddress.getHostName())) {
			sequenceMap.put(targetAddress.getHostName(), 0l);
		}
		long seq = sequenceMap.get(targetAddress.getHostName());
		msg.setSeq(seq);
		//System.out.println("to be sent: "+new String(msg.getData()));
		byte[] buf;
		try {
			buf = mapper.writeValueAsString(msg).getBytes();
			DatagramPacket p = new DatagramPacket(buf, buf.length, targetAddress, targetPort);
			dgSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
