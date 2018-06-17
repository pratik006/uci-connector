package com.prapps.chess.server.uci.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.tcp.common.Engine;

public class ServerConfigUtil {
	private ServerConfigDetail serverConfig;
	
	public ServerConfigUtil(InputStream is) throws IOException {
		ObjectMapper mapper = new  ObjectMapper();
		serverConfig = mapper.readValue(is, ServerConfigDetail.class);
		List<Engine> engines = serverConfig.getEngines().stream().filter(engine -> engine.isEnabled()).collect(Collectors.toList());
		engines.forEach(engine -> engine.setPort(serverConfig.getPort() + engine.getOffsetPort()));
		serverConfig.setEngines(engines);
	}
	
	public ServerConfigDetail getServerConfig() {
		return serverConfig;
	}
}
