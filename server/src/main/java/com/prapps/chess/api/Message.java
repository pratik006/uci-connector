package com.prapps.chess.api;

import java.util.Arrays;

public class Message implements Comparable<Message> {
	public static final int ENGINE_TYPE = 1;
	public static final int HANDSHAKE_TYPE = 2;
	public static final int AVAILABLE_ENGINE_TYPE = 3;
	
	private String engineId;
	private long seq;
	private byte[] data;
	private int type;
	
	public Message() { }
	
	public Message(long seq, byte[] data) {
		this.data = data;
		this.seq = seq;
	}
	
	public Message(long seq, String engineId, byte[] data) {
		this.data = data;
		this.engineId = engineId;
		this.seq = seq;
		this.type = ENGINE_TYPE;
	}
	
	public Message(long seq, String engineId, String data) {
		this.data = data.endsWith("\n") ? data.getBytes() : (data+"\n").getBytes();
		this.engineId = engineId;
		this.seq = seq;
		this.type = ENGINE_TYPE;
	}
	
	public Message(int type) {
		this.type = type;
	}
	
	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int compareTo(Message other) {
		return (int)(seq - other.getSeq());
	}

	@Override
	public String toString() {
		return "Message [engineId=" + engineId + ", seq=" + seq + ", data=" + Arrays.toString(data) + ", type=" + type
				+ "]";
	}
}