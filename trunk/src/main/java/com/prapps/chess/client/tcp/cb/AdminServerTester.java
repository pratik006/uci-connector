package com.prapps.chess.client.tcp.cb;

import static com.prapps.chess.uci.share.ProtocolConstants.CONN_SUCCESS_MSG;
import static com.prapps.chess.uci.share.ProtocolConstants.SET_PROTOCOL_TCP;
import static com.prapps.chess.uci.share.ProtocolConstants.START_MSG;
import static com.prapps.chess.uci.share.ProtocolConstants.SUCCESS_SET_PROTOCOL_TCP;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.NetworkRW;
import com.prapps.chess.uci.share.TCPNetworkRW;

public class AdminServerTester {

	private static Logger LOG = Logger.getLogger(AdminServerTester.class.getName());
	public static final int MAX_CONN_ATTEMPT = 5;
	

	private NetworkRW networkRW;
	private Properties config;
	private int adminPort;
	private InetAddress targetAddress;

	private void listen() throws FileNotFoundException, IOException, InterruptedException {		
		config = new Properties();
		config.load(new FileInputStream("src/test/resources/clientConfig.ini"));
		adminPort = Integer.parseInt(config.getProperty("admin_port"));		
		targetAddress = InetAddress.getByName(config.getProperty("target_ip"));		
		LOG.info("server: "+targetAddress.getHostName()+"\tport: "+adminPort);	
		networkRW = new TCPNetworkRW(new Socket(targetAddress, adminPort));
		networkRW.writeToNetwork("all_servers");
		System.out.println(networkRW.readFromNetwork());
	}

	
	public boolean connect() throws IOException {
		String connResp = null;
		 NetworkRW adminNetworkRW = new TCPNetworkRW(new Socket(targetAddress, adminPort));
		int connAttepmt = 0;
		do {
			connAttepmt++;
			adminNetworkRW.writeToNetwork(START_MSG);
			LOG.fine("sent connection initator packet :" + START_MSG + " to " + targetAddress + ":" + adminPort);
			connResp = adminNetworkRW.readFromNetwork();
			LOG.fine("Server :" + connResp);
			if (connResp.equalsIgnoreCase(CONN_SUCCESS_MSG)) {
				adminNetworkRW.writeToNetwork(SET_PROTOCOL_TCP);
				connResp = adminNetworkRW.readFromNetwork();
				if(SUCCESS_SET_PROTOCOL_TCP.equalsIgnoreCase(connResp)) {
					adminNetworkRW.writeToNetwork("exit");
					adminNetworkRW.close();
					return true;
				}
			}
		} while (connAttepmt < MAX_CONN_ATTEMPT);

		return false;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		/*System.out.println(System.getProperty("user.dir"));
		System.getProperties().put("Djava.util.logging.config.file", System.getProperty("user.dir")+"/logging.properties");*/
		LOG.finest("JAVA_HOME: "+System.getProperty("java.home"));
		LOG.finest("Logger initialized");
		LOG.finest("\n-------------------------------Starting UCIClient ---------------------------------------\n");
		AdminServerTester adapter = new AdminServerTester();
		adapter.listen();
	}

}
