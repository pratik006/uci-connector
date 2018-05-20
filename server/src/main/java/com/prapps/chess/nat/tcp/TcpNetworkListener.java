package com.prapps.chess.nat.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.prapps.chess.api.Message;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.nat.AbstractNetworkListener;
import com.prapps.chess.server.config.ServerConfig;

public class TcpNetworkListener extends AbstractNetworkListener {

	protected ServerSocket adminServerSocket;
	private Socket clientSocket;
	
	public TcpNetworkListener(SharedContext ctx, ServerConfig serverConfig) {
		super(ctx, serverConfig);
	}

	public void send(Message msg) {
		if (clientSocket != null && clientSocket.isConnected()) {
			try {
				//System.out.println(new String(msg.getData()));
				clientSocket.getOutputStream().write((ctx.getObjectMapper().writeValueAsString(msg)+"\n").getBytes());
				clientSocket.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			adminServerSocket = new ServerSocket(serverConfig.getTcpPort());
			clientSocket = adminServerSocket.accept();
			String line = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			while ((line = reader.readLine()) != null) {
				Message msg = ctx.getObjectMapper().readValue(line, Message.class);
				if (engineIds.contains(msg.getEngineId()))
					engineController.addMessage(msg);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
