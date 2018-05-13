package com.prapps.chess.api.config;

import java.util.List;

import com.prapps.chess.api.StunServer;

public class BaseConfig {
	private String serverId;
	private String clientId;
	private UdpConfig udpConfig;
	private TcpConfig tcpConfig;
	private int tcpPort;
	private String protocol;
	private String externalHost;
	
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public UdpConfig getUdpConfig() {
		return udpConfig;
	}
	public void setUdpConfig(UdpConfig udpConfig) {
		this.udpConfig = udpConfig;
	}
	public int getTcpPort() {
		return tcpPort;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	public String getExternalHost() {
		return externalHost;
	}
	public void setExternalHost(String externalHost) {
		this.externalHost = externalHost;
	}
	public TcpConfig getTcpConfig() {
		return tcpConfig;
	}
	public void setTcpConfig(TcpConfig tcpConfig) {
		this.tcpConfig = tcpConfig;
	}

	public static class UdpConfig {
		int selectedIndex;
		int sourcePort;
		int refreshInterval;
		int socketTimeout;
		List<StunServer> stunServers;
		
		public int getSelectedIndex() {
			return selectedIndex;
		}
		public void setSelectedIndex(int selectedIndex) {
			this.selectedIndex = selectedIndex;
		}
		public int getSourcePort() {
			return sourcePort;
		}
		public void setSourcePort(int sourcePort) {
			this.sourcePort = sourcePort;
		}
		public int getSocketTimeout() {
			return socketTimeout;
		}
		public void setSocketTimeout(int socketTimeout) {
			this.socketTimeout = socketTimeout;
		}
		public List<StunServer> getStunServers() {
			return stunServers;
		}
		public void setStunServers(List<StunServer> stunServers) {
			this.stunServers = stunServers;
		}
		public int getRefreshInterval() {
			return refreshInterval;
		}
		public void setRefreshInterval(int refreshInterval) {
			this.refreshInterval = refreshInterval;
		}
	}
	
	public static class TcpConfig {
		private int port;

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}

}
