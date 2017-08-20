package com.prapps.chess.tcp.server;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

public class EngineStarter {

	public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		config.load(new FileInputStream("remote-client.ini"));
		int engineStartPort = Integer.parseInt(config.getProperty("engine-start-port"));
		InetAddress ip = InetAddress.getByName(config.getProperty("server-ip"));
		
		ChessEngine cb = new ChessEngine(config.getProperty("1"), null);
		cb.start();
		while (cb.isReady()) {
			Socket socket = new Socket(ip, engineStartPort + 1);
			System.out.println(socket);
			cb.connect(socket);
			cb.start();
		}		
	}

}
