package com.prapps.chess;

import java.io.IOException;

import com.prapps.chess.server.uci.tcp.AdminServer;
import com.prapps.chess.server.uci.tcp.ui.MainFrame;

public class Starter {

	public static void main(String[] args) throws IOException {
		if(null == args || args.length == 0) {
			AdminServer.main(args);
		}
		else if("serverui".equals(args[0])) {
			MainFrame.main(args);
		}
	}

}
