package com.prapps.chess.server.uci.tcp;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServerConfigUtil {

	private ServerConfig serverConfig;
	
	public ServerConfigUtil(InputStream is) throws IOException {
		ObjectMapper mapper = new  ObjectMapper();
		serverConfig = mapper.readValue(is, ServerConfig.class);
	}
	
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
}
