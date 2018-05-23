package com.prapps.chess.api.config;

import java.util.List;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.StunServer;

public class BaseConfig {
	private String id;
	private String otherId;
	private UdpConfig udpConfig;
	private TcpConfig tcpConfig;
	private int tcpPort;
	private String protocol;
	private String externalHost;
	private Long timeoutDuration;
	private boolean client;
	
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

	public Long getTimeoutDuration() {
		return timeoutDuration;
	}
	public void setTimeoutDuration(Long timeoutDuration) {
		this.timeoutDuration = timeoutDuration;
	}

	public boolean isClient() {
		return client;
	}
	public void setClient(boolean client) {
		this.client = client;
	}

	public static class UdpConfig {
		int selectedIndex;
		int sourcePort;
		int refreshInterval;
		int socketTimeout;
		List<StunServer> stunServers;
		private NatDetail nat;
		
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
		public NatDetail getNat() {
			return nat;
		}
		public void setNat(NatDetail nat) {
			this.nat = nat;
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
