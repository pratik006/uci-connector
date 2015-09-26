package com.prapps.chess.server.uci.tcp.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.prapps.chess.server.uci.tcp.AdminServer;
import com.prapps.chess.server.uci.tcp.EngineServer;
import com.prapps.chess.server.uci.tcp.Server;
import com.prapps.chess.server.uci.tcp.ServerConfig;
import com.prapps.chess.server.uci.thread.AbstractRunnable;
import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.uci.share.NetworkRW;
import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.TCPNetworkRW;

public class InteractiveThread extends AbstractRunnable {

	private static final Logger LOG = Logger.getLogger(InteractiveThread.class.getName());
	protected ServerSocket adminServerSocket;
	protected NetworkRW adminNetworkRW;
	private ServerConfig serverConfig;
	private AdminServer adminServer;

	public InteractiveThread(int port, AdminServer adminServer, ServerConfig serverConfig) throws IOException {
		adminServerSocket = new ServerSocket(port);
		this.adminServer = adminServer;
		this.serverConfig = serverConfig;
		setState(State.New);
	}

	public void run() {
		try {
			LOG.finest("--------------------------------------");
			LOG.info("waiting for connection..." + adminServerSocket.getLocalPort());
			do {
				setState(State.Waiting);
				adminNetworkRW = new TCPNetworkRW(adminServerSocket.accept());

				LOG.info("Connection received from " + adminNetworkRW.getAddress().getHostName());
				String request = null;
				String sentMsg = null;
				do {
					setState(State.Connected);
					request = adminNetworkRW.readFromNetwork();
					setState(State.Running);
					LOG.info("Client: " + request);
					sentMsg = "";
					if (null != request) {
						if (ProtocolConstants.START_MSG.equals(request)) {
							sentMsg = ProtocolConstants.CONN_SUCCESS_MSG;
							//connected = true;
						} 
						else if (ProtocolConstants.GET_AVAILABLE_ENGINES.equals(request)) {
							for (Server server : serverConfig.getServers()) {
								sentMsg += server.getName() + ":" + server.getPort() + ";";
							}
						}
						else if (ProtocolConstants.GET_ENGINE_STATUSES.equals(request)) {
							for (Entry<String, EngineServer> entry : adminServer.getServerDetails().entrySet()) {
								EngineServer server = entry.getValue();
								sentMsg += server.getName() + ":" + server.getPort() + ":" + server.getState() +";";
							}
						 }
						else if(ProtocolConstants.SHUT_DOWN.equals(request)) {
							LOG.info("shutting down...");
							sentMsg = ProtocolConstants.CLOSE_MSG;
							adminNetworkRW.writeToNetwork(sentMsg);
							adminServer.close();
							StringBuffer output = new StringBuffer();
							String cmd = "echo 'peed' | sudo -S poweroff";
							Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});
							try {
								process.waitFor();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							BufferedReader reader = 
				                            new BufferedReader(new InputStreamReader(process.getInputStream()));
				 
				                        String line = "";			
							while ((line = reader.readLine())!= null) {
								output.append(line + "\n");
							}
							System.err.println(output);
						}
						else if(request.indexOf(ProtocolConstants.RESTART_ENGINE) != -1) {
							LOG.info("restarting engine...");
							String engineId = request.split("=")[1];
							adminServer.restartEngine(engineId);
						}
						LOG.finest("Writing to Socket: "+sentMsg);
						adminNetworkRW.writeToNetwork(sentMsg);
					}
				} while (null != request && !"exit".equals(request));
				adminNetworkRW.close();
			} while (getState() == State.Running || getState() == State.Connected);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			setState(State.Closed);
		}
		setState(State.Closed);
	}
}
