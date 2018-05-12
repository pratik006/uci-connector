package com.prapps.chess.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.server.Engine;

public enum ServerLoader {
	INSTANCE;
	private ServerConfig serverConfig;
	
	private ServerLoader() {
		try(FileInputStream fis = new FileInputStream("serverConfig.json")) {
			byte[] buf = new byte[1024*100];
			int readLen = -1;
			StringBuilder json = new StringBuilder(); 
			while ((readLen = fis.read(buf)) != -1) {
				json.append(new String(buf, 0, readLen));
			}
			
			ObjectMapper mapper = new ObjectMapper();
			serverConfig =  mapper.readValue(json.toString(), ServerConfig.class);
			List<Engine> servers = serverConfig.getServers().stream().filter(server -> new File(server.getPath()).exists() && server.isEnabled() && server.selfTest()).collect(Collectors.toList());
			serverConfig.setServers(servers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Engine> getServers() {
		return serverConfig.getServers();
	}
	
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(ServerLoader.INSTANCE.getServers().size());
	}
}
