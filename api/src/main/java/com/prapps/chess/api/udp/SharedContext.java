package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.config.BaseConfig;

import de.javawi.jstun.header.MessageHeader;

public class SharedContext {
	private Logger LOG = LoggerFactory.getLogger(SharedContext.class);
	public static int TIME_DIFF_ALLOWED = 5*60*1000;
	
	private static DatagramSocket socket;
	private String id;
	private AtomicReference<MessageHeader> sendMHRef = new AtomicReference<MessageHeader>();
	private BaseConfig baseConfig;
	private AtomicBoolean exit = new AtomicBoolean(false);
	private AtomicReference<NatDetail> nat = new AtomicReference<>();
	private AtomicReference<State> connectionState = new AtomicReference<State>(new State());
	private List<PacketListener> listeners;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	/*public static DatagramSocket getSocket() {
		return socket;
	}*/
	public void setSocket(DatagramSocket socket) {
		SharedContext.socket = socket;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public AtomicReference<MessageHeader> getSendMHRef() {
		return sendMHRef;
	}
	public void setSendMHRef(AtomicReference<MessageHeader> sendMHRef) {
		this.sendMHRef = sendMHRef;
	}
	public BaseConfig getBaseConfig() {
		return baseConfig;
	}
	public void setBaseConfig(BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}
	public AtomicBoolean getExit() {
		return exit;
	}
	public void setExit(AtomicBoolean exit) {
		this.exit = exit;
	}
	public AtomicReference<NatDetail> getNat() {
		return nat;
	}
	public void setNat(AtomicReference<NatDetail> nat) {
		this.nat = nat;
	}
	public AtomicReference<State> getConnectionState() {
		return connectionState;
	}
	public void setConnectionState(AtomicReference<State> connectionState) {
		this.connectionState = connectionState;
	}
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	public List<PacketListener> getListeners() {
		return listeners;
	}
	public void setListeners(List<PacketListener> listeners) {
		this.listeners = listeners;
	}
	
	private void send(DatagramPacket packet) throws IOException {
		LOG.trace("Sending Packet: "+new String(packet.getData()));
		socket.send(packet);
	}
	
	public void send(Message msg) throws IOException {
		msg.setTimestamp(System.currentTimeMillis());
		if (nat.get().getHost() != null && nat.get().getPort() != 0) {
			msg.setPort(nat.get().getPort());
			msg.setHost(nat.get().getHost());
		}
		
		String json = getObjectMapper().writeValueAsString(msg);
		DatagramPacket p = new DatagramPacket(json.getBytes(), json.getBytes().length);
		p.setPort(msg.getPort());
		p.setAddress(InetAddress.getByName(msg.getHost()));
		send(p);
	}
	
	public void send(int msgType) throws IOException {
		send(new Message(msgType));
	}
	
	public void connectAndSend(DatagramPacket packet) throws IOException {
		synchronized (socket) {
			socket.connect(packet.getAddress(), packet.getPort());
			socket.send(packet);
			socket.disconnect();
			LOG.trace("Packet sent to "+packet.getAddress().getHostName()+":"+packet.getPort()+"\tData: "+new String(packet.getData()));
		}
	}
	
	public void receive(DatagramPacket p) throws IOException {
		socket.receive(p);
	}
	
	public void close() {
		socket.close();
	}
}
