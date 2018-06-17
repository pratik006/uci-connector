package com.prapps.chess.tcp.common;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {
	private String id;
	private String ip;
	private String localIp;
	private int port;
	private String externalServerUrl;
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

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public List<Engine> getEngines() {
		if (engines == null) {
			engines = new ArrayList<Engine>();
		}
		return engines;
	}

	public void setEngines(List<Engine> engines) {
		this.engines = engines;
	}

	public String getExternalServerUrl() {
		return externalServerUrl;
	}

	public void setExternalServerUrl(String externalServerUrl) {
		this.externalServerUrl = externalServerUrl;
	}
}
