package com.prapps.chess.client.tcp.cb;

import com.prapps.chess.tcp.common.ServerConfig;

public class ClientConfig {
	private String id;
	private String serverId;
	private String engineId;
	private String externalServerUrl;
	private String networkPref;
	private ServerConfig server;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getExternalServerUrl() {
		return externalServerUrl;
	}
	public void setExternalServerUrl(String externalServerUrl) {
		this.externalServerUrl = externalServerUrl;
	}
	public String getNetworkPref() {
		return networkPref;
	}
	public void setNetworkPref(String networkPref) {
		this.networkPref = networkPref;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public String getEngineId() {
		return engineId;
	}
	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}
	public ServerConfig getServer() {
		return server;
	}
	public void setServer(ServerConfig server) {
		this.server = server;
	}
}
