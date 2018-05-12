package com.prapps.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.server.config.ServerConfig;
import com.prapps.chess.server.config.ServerLoader;

public class TcpTester {

	public static void main(String[] args) throws UnknownHostException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ServerConfig serverConfig = ServerLoader.INSTANCE.getServerConfig();
		Socket s = new Socket("localhost", serverConfig.getTcpPort());
		s.getOutputStream().write((mapper.writeValueAsString(new Message(1, "critter", "uci"))+"\n").getBytes());
		s.getOutputStream().write((mapper.writeValueAsString(new Message(1, "critter", "isready"))+"\n").getBytes());
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
		System.out.println(new String(mapper.readValue(reader.readLine(), Message.class).getData()));
		System.out.println(new String(mapper.readValue(reader.readLine(), Message.class).getData()));
		System.out.println(new String(mapper.readValue(reader.readLine(), Message.class).getData()));
	}

}
