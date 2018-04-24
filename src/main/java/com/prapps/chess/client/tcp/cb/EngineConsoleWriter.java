package com.prapps.chess.client.tcp.cb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.TCPNetworkRW;

public class EngineConsoleWriter implements Runnable {
	private static Logger LOG = Logger.getLogger(EngineConsoleWriter.class.getName());
	
	private Boolean exit;
	private TCPNetworkRW networkRW;
	
	public EngineConsoleWriter(InetAddress targetAddress, int targetPort, Boolean exit) throws IOException {
		this.exit = exit;
		networkRW = new TCPNetworkRW(new Socket(targetAddress, targetPort));
	}
	
	@Override
	public void run() {
		String serverMsg = null;
		while (!exit) {
			try {
				serverMsg = networkRW.readFromNetwork();
				LOG.finest("Server: "+serverMsg);
				if("exit".equalsIgnoreCase(serverMsg)) {
					networkRW.close();
					exit = true;
				}
				else {
					System.out.write(serverMsg.getBytes());
					System.out.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOG.finest("Closing CB Writer");
		try {
			System.out.write("quit".getBytes());
			//System.in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
