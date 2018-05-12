package com.prapps.chess;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.prapps.chess.nat.tcp.TcpNetworkListener;
import com.prapps.chess.nat.udp.UdpListener;
import com.prapps.chess.server.config.ServerConfig;
import com.prapps.chess.server.config.ServerLoader;

public class ServerStarter {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerConfig serverConfig = ServerLoader.INSTANCE.getServerConfig();
		AtomicBoolean exit = new AtomicBoolean(false);
		if ("tcp".equals(serverConfig.getProtocol())) {
			new Thread(new TcpNetworkListener(serverConfig, exit)).start();
		} else if ("udp".equals(serverConfig.getProtocol())) {
			new Thread(new UdpListener(exit, serverConfig)).start();
		}
	}

}
