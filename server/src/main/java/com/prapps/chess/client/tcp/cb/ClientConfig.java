package com.prapps.chess.client.tcp.cb;

public class ClientConfig {
	private String targetIp;
	private int targetPort;
	private int adminPort;
	private String externalServerUrl;
	private String networkPref;
	
	public String getTargetIp() {
		return targetIp;
	}
	public void setTargetIp(String targetIp) {
		this.targetIp = targetIp;
	}
	public int getTargetPort() {
		return targetPort;
	}
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}
	public int getAdminPort() {
		return adminPort;
	}
	public void setAdminPort(int adminPort) {
		this.adminPort = adminPort;
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
}
