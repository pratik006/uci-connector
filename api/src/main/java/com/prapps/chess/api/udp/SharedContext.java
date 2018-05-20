package com.prapps.chess.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
	
	private DatagramSocket socket;
	private String id;
	private String otherId;
	private AtomicReference<MessageHeader> sendMHRef = new AtomicReference<MessageHeader>();
	private BaseConfig baseConfig;
	private AtomicBoolean exit = new AtomicBoolean(false);
	private AtomicReference<NatDetail> nat = new AtomicReference<>();
	private AtomicReference<State> connectionState = new AtomicReference<State>(new State());
	private List<PacketListener> listeners;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String uuid;
	private AtomicLong seq = new AtomicLong(0);
	private AtomicLong readSeq = new AtomicLong(0);
	
	public DatagramSocket getSocket() {
		return socket;
	}
	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOtherId() {
		return otherId;
	}
	public void setOtherId(String otherId) {
		this.otherId = otherId;
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
	public int getState() { return connectionState.get() != null ? connectionState.get().getState() : -1; }
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
		socket.send(packet);
		LOG.trace("Packet sent to "+packet.getPort()+" Data: "+new String(packet.getData()));
		//LOG.trace("From "+socket.getLocalAddress().getHostName()+":"+socket.getLocalPort());
	}
	
	public void send(Message msg) throws IOException {
		msg.setTimestamp(System.currentTimeMillis());
		if (nat.get() != null && nat.get().getHost() != null && nat.get().getPort() != 0) {
			msg.setPort(nat.get().getPort());
			msg.setHost(nat.get().getHost());
		}
		
		int port = msg.getPort();
		String host = msg.getHost();
		msg.setHost(null);
		msg.setPort(-1);
		String json = getObjectMapper().writeValueAsString(msg);
		DatagramPacket p = new DatagramPacket(json.getBytes(), json.getBytes().length);
		p.setPort(port);
		p.setAddress(InetAddress.getByName(host));
		send(p);
	}
	
	public void send(int msgType) throws IOException {
		Message msg = new Message(msgType);
		if (uuid == null) {
			generateUuid();
		}
		msg.setData(uuid.getBytes());
		send(msg);
	}
	
	public void connectAndSend(DatagramPacket packet) throws IOException {
		send(packet);
	}
	
	public void receive(DatagramPacket p) throws IOException {
		socket.receive(p);
	}
	
	public void close() {
		socket.close();
	}
	
	private String generateUuid() {
		this.uuid = UUID.randomUUID().toString();
		LOG.debug("UUID: "+this.uuid);
		return uuid;
	}
	
	public boolean isSameUuid(Message msg) {
		if (msg.getData() == null) {
			return false;
		}
		
		String otherUuid = new String(msg.getData());
		//LOG.debug(this.uuid+" vs "+otherUuid);
		return uuid != null && this.uuid.equals(otherUuid);
	}
	
	public void resetUuid() {
		uuid = null;
	}
	
	public void resetSeq() {
		synchronized (seq) {
			this.seq.set(0);	
		}
		readSeq.set(0);
	}
	
	public Long getSeq() {
		return seq.get();
	}
	
	public Long incrementSeq() {
		return this.seq.incrementAndGet();
	}
	
	public Long getReadSeq() {
		return seq.get();
	}
	
	public Long incrementReadSeq() {
		return this.seq.incrementAndGet();
	}
}
