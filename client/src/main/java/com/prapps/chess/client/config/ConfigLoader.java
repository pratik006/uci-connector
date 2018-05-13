package com.prapps.chess.client.config;

import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public enum ConfigLoader {
	INSTANCE;
	private ClientConfig clientConfig;
	
	private ConfigLoader() {
		try(FileInputStream fis = new FileInputStream("clientConfig.json")) {
			byte[] buf = new byte[1024*100];
			int readLen = -1;
			StringBuilder json = new StringBuilder(); 
			while ((readLen = fis.read(buf)) != -1) {
				json.append(new String(buf, 0, readLen));
			}
			
			ObjectMapper mapper = new ObjectMapper();
			clientConfig =  mapper.readValue(json.toString(), ClientConfig.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ClientConfig getClientConfig() {
		return clientConfig;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(ConfigLoader.INSTANCE);
	}
}
