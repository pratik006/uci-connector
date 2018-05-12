package com.prapps.chess.client.tcp.cb;

import java.util.Arrays;

public class ServerDetails {

	private boolean connected;
	private String[][] servers;
	
	public ServerDetails(String serversStr) {
		String[] servers = serversStr.split(";");
		this.servers = new String[servers.length][3];
		int ctr = 0;
		for(String row : servers) {
			this.servers[ctr++] = row.split(":");
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String[][] getServers() {
		return servers;
	}

	public void setServers(String[][] servers) {
		this.servers = servers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServerDetails [connected=" + connected + ", servers=");
		for(String[] server : servers) {
			sb.append(Arrays.toString(server)+", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
}
