package com.prapps.chess.client;

import com.prapps.chess.client.UdpClient;

public class ClientStarter {

	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 0 && "tcp".equals(args[0])) {
			TcpTester.main(args);
		} else {
			UdpClient.main(args);
		}
	}

}
