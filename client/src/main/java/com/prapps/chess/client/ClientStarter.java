package com.prapps.chess.client;

import java.io.IOException;

public class ClientStarter {

	public static void main(String[] args) throws IOException {
		if (args != null && args.length > 0 && "tcp".equals(args[0])) {
			TcpTester.main(args);
		} else {
			UdpClient.main(args);
		}
	}

}
