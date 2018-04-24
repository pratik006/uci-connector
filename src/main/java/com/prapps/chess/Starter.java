package com.prapps.chess;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.prapps.chess.client.tcp.cb.ChessbaseClientAdapter;
import com.prapps.chess.client.tcp.ui.TCPClientUtil;
import com.prapps.chess.server.uci.tcp.AdminServer;
import com.prapps.chess.server.uci.tcp.ui.MainFrame;

public class Starter {

	public static void main(String[] args) throws IOException, JAXBException {
		if(null == args || args.length == 0) {
			AdminServer.main(args);
		}
		else if("serverui".equals(args[0])) {
			MainFrame.main(args);
		}
		else if("clientui".equals(args[0])) {
			TCPClientUtil.main(args);
		}
		else if("client".equals(args[0])) {
			try {
				ChessbaseClientAdapter.main(args);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
