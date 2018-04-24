package com.prapps.chess.nat;

public class NatDetail {

	private String id;
	private String host;
	private int port;
	private Long lastUpdated;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Long getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	public String toString() {
		return "{id: "+id+", host: "+host+", port: "+port+", lastUpdated: "+lastUpdated+"}";
	}
}
