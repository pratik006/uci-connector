package com.prapps.chess.client.tcp.cb;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.TCPNetworkRW;
import com.prapps.chess.uci.share.UCIUtil;

public class ServerConnector {

	private static Logger LOG = Logger.getLogger(ServerConnector.class.getName());
	private static ServerConnector serverConnector;
	private String ip;
	private int port;
	private boolean initialized;
	private Socket socket;
	private Properties config;
	
	private ServerConnector() {
		
	}
	
	public static ServerConnector getInstance() throws IOException {
		if(serverConnector == null) {
			serverConnector = new ServerConnector();
			serverConnector.init();
		}
		return serverConnector;
	}
	
	private void init() throws IOException {
		config = new Properties();
		config.load(new FileInputStream("clientConfig.ini"));
		String externalServerUrl = config.getProperty("external_server_ip");
		LOG.fine("externalServerUrl: "+externalServerUrl);
		String ip = UCIUtil.getServerIP(externalServerUrl);
		int port = Integer.parseInt(UCIUtil.getExternalParam(externalServerUrl, "port"));
		try {
			LOG.fine("Connecting to "+ip+":"+port);
			socket = new Socket(ip, port);
			if(!connect())
				throw new IOException("cannot connect.");
		}
		catch(IOException ex) {
			LOG.info(ex.getMessage());
			ip = UCIUtil.getExternalParam(externalServerUrl, "localip");
			LOG.fine("Connecting to "+ip+":"+port);
			try {
				socket = new Socket(ip, port);
				if(!connect())
					throw new IOException("cannot connect.");
			}
			catch(IOException ex2) {
				LOG.info(ex.getMessage());
				ip = UCIUtil.getLocalIP();
				LOG.fine("Connecting to "+ip+":"+port);
				socket = new Socket(ip, port);
				if(!connect())
					throw new IOException("cannot connect.");
			}
			
		}
		initialized = true;
		
		LOG.info("connected");
	}
	
	public boolean connect() throws IOException {				
		LOG.info("connecting to : "+socket.getInetAddress()+"\tport: "+socket.getPort());
		final TCPNetworkRW networkRW = new TCPNetworkRW(socket);
		networkRW.writeToNetwork(ProtocolConstants.START_MSG);
		String response = networkRW.readFromNetwork();
		LOG.info("Server: "+response);
		return ProtocolConstants.CONN_SUCCESS_MSG.equals(response);
	}
	
	public String sendMsg(String msg) throws IOException {
		InetAddress targetAddress = null;
		targetAddress = InetAddress.getByName(ip);		
		LOG.info("server: "+targetAddress.getHostName()+"\tport: "+port);
		if(!socket.isConnected() || socket.isClosed()) {
			init();
		}
		final TCPNetworkRW networkRW = new TCPNetworkRW(socket);
		networkRW.writeToNetwork(ProtocolConstants.START_MSG);
		String response = networkRW.readFromNetwork();
		LOG.finest("Server: "+response);
		networkRW.writeToNetwork(msg);
		response = networkRW.readFromNetwork();
		return response;
	}
	
	public TCPNetworkRW connect(String ip, int port) throws IOException {
		InetAddress targetAddress = null;
		targetAddress = InetAddress.getByName(ip);		
		LOG.info("server: "+targetAddress.getHostName()+"\tport: "+port);
		socket = new Socket(targetAddress, port);
		final TCPNetworkRW networkRW = new TCPNetworkRW(socket);
		networkRW.writeToNetwork(ProtocolConstants.START_MSG);
		String connected = networkRW.readFromNetwork();
		LOG.finest("Server: "+connected);
		this.ip = ip;
		this.port = port;
		return networkRW;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void close() throws IOException {
		socket.close();
	}

	public boolean isConnected() {
		return socket.isConnected();
	}
	
	public static void main(String[] args) throws IOException {
		ServerConnector connector = ServerConnector.getInstance();
		//ServerDetails serverDetails = connector.connect("localhost",8080);
		//System.err.println(serverDetails);
	}
}
