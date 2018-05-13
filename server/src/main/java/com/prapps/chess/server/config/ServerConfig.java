package com.prapps.chess.server.config;

import java.util.List;

import com.prapps.chess.api.config.BaseConfig;
import com.prapps.chess.server.Engine;

public class ServerConfig extends BaseConfig {
	private List<Engine> servers;
	
	public List<Engine> getServers() {
		return servers;
	}
	public void setServers(List<Engine> servers) {
		this.servers = servers;
	}
}
