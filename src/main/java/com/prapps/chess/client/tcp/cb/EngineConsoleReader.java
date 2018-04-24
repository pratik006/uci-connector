package com.prapps.chess.client.tcp.cb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.TCPNetworkRW;

public class EngineConsoleReader implements Runnable {
	private static Logger LOG = Logger.getLogger(EngineConsoleWriter.class.getName());
	
	private Boolean exit;
	private TCPNetworkRW networkRW;
	
	public EngineConsoleReader(InetAddress targetAddress, int targetPort, Boolean exit) throws IOException {
		networkRW = new TCPNetworkRW(new Socket(targetAddress, targetPort));
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];
		try {
			int readLen = buffer.length;
			while (!exit && (readLen = System.in.read(buffer, 0, buffer.length)) != -1) {
				LOG.finest("Chessbase: "+new String(buffer, 0, readLen));
				networkRW.writeToNetwork(Arrays.copyOfRange(buffer, 0, readLen));
				if (new String(buffer, 0, readLen).contains("quit")) {
					exit = true;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		LOG.finest("closing Engine Console Reader");
	}

}
