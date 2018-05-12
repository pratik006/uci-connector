package com.prapps.chess.api;

import java.util.List;

public class ClientConfig {
	private UdpConfig udpConfig;
	private TcpConfig tcpConfig;
	private int tcpPort;
	private String protocol;
	private String externalHost;
	
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
