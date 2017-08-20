package com.prapps.chess.client.http;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

public class Message implements Serializable, Comparable<Message> {

	private static final long serialVersionUID = 1L;

	public Message() {}

	public Message(int id, String msg) {
		this.id = id;
		this.msg = msg;
	}

	private int id;
	private String msg;
	private Calendar time;
	private String userName;
	private String ip;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Message)) {
			return false;
		}
		Message otherMsg = (Message) other;
		return id == otherMsg.getId();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int compareTo(Message other) {
		return (int)(time.getTimeInMillis() - other.getTime().getTimeInMillis());
	}
}
