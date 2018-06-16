package com.prapps.chess.client.tcp.cb;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.tcp.common.ServerConfig;
import com.prapps.chess.tcp.common.ServerConfig.Engine;

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
			serverConfig.setPort(root.get("port").asInt());
			
			JsonNode node = root.get("engines").get(config.getEngineId());
			Engine engine = serverConfig.new Engine(config.getEngineId(), node.get("offsetPort").asInt());
			serverConfig.setEngines(Collections.singletonList(engine));
			config.setServer(serverConfig);
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
		System.out.println(ClientConfigLoader.INSTANCE.getConfig().getServer().getIp());
	}
}
