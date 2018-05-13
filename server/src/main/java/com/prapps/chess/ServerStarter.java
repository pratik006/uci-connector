package com.prapps.chess;

import java.io.IOException;

import com.prapps.chess.nat.tcp.TcpNetworkListener;
import com.prapps.chess.nat.udp.UdpListener;
import com.prapps.chess.server.config.ServerConfig;
import com.prapps.chess.server.config.ServerLoader;

public class ServerStarter {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerConfig serverConfig = ServerLoader.INSTANCE.getServerConfig();
		if ("tcp".equals(serverConfig.getProtocol())) {
			new Thread(new TcpNetworkListener(null, serverConfig)).start();
		} else if ("udp".equals(serverConfig.getProtocol())) {
			new Thread(new UdpListener(serverConfig)).start();
		}
	}

}
