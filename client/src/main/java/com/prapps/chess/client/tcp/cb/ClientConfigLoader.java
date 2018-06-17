package com.prapps.chess.client.tcp.cb;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.tcp.common.Engine;
import com.prapps.chess.tcp.common.ServerConfig;

public enum ClientConfigLoader {
	INSTANCE("clientConfig.json");
	private ClientConfig config;
	
	private ClientConfigLoader(String filename) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			config = mapper.readValue(new FileInputStream(filename), ClientConfig.class);
			String extUrl = config.getExternalServerUrl();
			String targetUrl = extUrl.split(".json")[0] + "/" + config.getServerId() + ".json";
			ServerConfig serverConfig = new ServerConfig();
			serverConfig.setId(config.getServerId());
			JsonNode root = mapper.readTree(new URL(targetUrl));
			serverConfig.setIp(root.get("ip").asText());
			serverConfig.setLocalIp(root.get("localIp").asText());
			serverConfig.setPort(root.get("port").asInt());
			
			if (root.get("engines").isArray()) {
				for (JsonNode node : root.get("engines")) {
					if (node.get("id").asText().equalsIgnoreCase(config.getEngineId())) {
						Engine engine = new Engine(config.getEngineId(), node.get("offsetPort").asInt());
						engine.setPort(serverConfig.getPort() + engine.getOffsetPort());
						serverConfig.setEngines(Collections.singletonList(engine));
						config.setServer(serverConfig);
						break;
					}
				}
			}
			
			if (config.getServer() == null) {
				throw new IOException("engine id not found");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ClientConfig getConfig() throws IOException {
		if (config == null)  {
			throw new IOException("client config file not found");
		}
		
		return config;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(ClientConfigLoader.INSTANCE.getConfig().getServer().getEngines().get(0).getOffsetPort());
	}
}
