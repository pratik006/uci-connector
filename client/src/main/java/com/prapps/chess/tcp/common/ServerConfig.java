package com.prapps.chess.tcp.common;

import java.util.List;

public class ServerConfig {
	private String id;
	private String ip;
	private int port;
	private List<Engine> engines;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public List<Engine> getEngines() {
		return engines;
	}

	public void setEngines(List<Engine> engines) {
		this.engines = engines;
	}

	public class Engine {
		private int offsetPort;
		private String id;
		
		public Engine(String id, int offsetPort) {
			this.id = id;
			this.offsetPort = offsetPort;
		}
		
		public int getOffsetPort() {
			return offsetPort;
		}
		public void setOffsetPort(int offsetPort) {
			this.offsetPort = offsetPort;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}
}
