package com.prapps.chess.server.uci.tcp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import com.prapps.chess.server.uci.tcp.thread.InteractiveThread;
import com.prapps.chess.server.uci.tcp.thread.MailerThread;
import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.uci.share.NetworkRW;

public class AdminServer {

	private static Logger LOG = Logger.getLogger(AdminServer.class.getName());
	
	private ServerConfig serverConfig;
	protected Server[] server;
	//protected static Properties config;
	protected int cores = -1;
	protected static int adminPort;
	public boolean exit;
	protected boolean engineStarted = false;
	protected boolean connected = false;
	protected Process p;
	//protected ServerSocket adminServerSocket;
	protected NetworkRW adminNetworkRW;

	protected String enginePath;
	protected char[] password;
	protected static Map<String, EngineServer> servers = new HashMap<String, EngineServer>();
	protected static List<Thread> serverThreads = new ArrayList<Thread>();
	
	protected Thread guiToEngineWriterThread;
	protected Thread engineToGUIWriterThread;
	private Thread adminThread;
	
	public AdminServer(String configPath) throws FileNotFoundException, JAXBException {
		InputStream is = new FileInputStream(configPath);
		ServerConfigUtil serverConfigUtil = new ServerConfigUtil(is);
		serverConfig = serverConfigUtil.getServerConfig();
	}

	public AdminServer(int port) throws FileNotFoundException, IOException {
		cores = Runtime.getRuntime().availableProcessors();
		LOG.info("available cores: " + cores);
		if (cores > 1)
			cores = cores - 1;
		LOG.info("usable cores: " + cores);
		//enginePath = config.getProperty(String.valueOf(port));
	}

	public void start() throws IOException {
		//AdminServer.config = config;
		LOG.info("server starting");
		adminPort = serverConfig.getAdminPort();
		//protocol = config.getProperty("protocol");
		LOG.info("protocol type: "+serverConfig.getProtocol());
		LOG.info("admin port: "+adminPort);
		//adminServerSocket = new ServerSocket(adminPort);
		//Thread mailer = new Thread(new MailerThread(serverConfig));mailer.start();
		
		new Thread(new Runnable() {
			public void run() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						LOG.fine("Admin command: "+line);
						if (line.startsWith(":q")) {
							exit = true;
							adminThread.interrupt();
							for (Entry<String, EngineServer> entry : servers.entrySet()) {
								entry.getValue().stateClosing = true;
							}							
							break;
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		adminThread = new Thread(new InteractiveThread(adminPort, this, serverConfig));adminThread.start();
		/*adminThread = new Thread(new Runnable() {
			public void run() {
				try {
					System.err.println("--------------------------------------");
					LOG.info("waiting for connection..." + adminServerSocket.getLocalPort());
					do {
						adminNetworkRW = new TCPNetworkRW(adminServerSocket.accept());

						LOG.info("Connection received from " + adminNetworkRW.getAddress().getHostName());
						String request = null;
						String sentMsg = null;
						do {
							request = adminNetworkRW.readFromNetwork();
							LOG.info("Client: " + request);
							sentMsg = "";
							if (null != request) {
								if (ProtocolConstants.START_MSG.equals(request)) {
									sentMsg = ProtocolConstants.CONN_SUCCESS_MSG;
									connected = true;
								} else if (ProtocolConstants.GET_AVAILABLE_ENGINES.equals(request)) {
									for (Server server : serverConfig.getServers()) {
										sentMsg += server.getName() + ":" + server.getPort() + ";";
									}
								}
								else if(ProtocolConstants.SHUT_DOWN.equals(request)) {
									LOG.info("shutting down...");
									sentMsg = ProtocolConstants.CLOSE_MSG;
									adminNetworkRW.writeToNetwork(sentMsg);
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
								LOG.finest("Writing to Socket: "+sentMsg);
								adminNetworkRW.writeToNetwork(sentMsg);
							}
						} while (null != request && !"exit".equals(request));
					} while (!exit);
				} catch (IOException e) {
					e.printStackTrace();
				} 

			}
		}, "KeyboardListernerTherad");
		adminThread.start();*/
		
		LOG.info("Admin server started");
		/*while(!exit)
			handshake();*/
		initEngines();
		LOG.fine("Admin server closed");
	}
	
	public void restartEngine(String engineId) {
		LOG.info("Restart Engine: "+engineId);
		servers.get(engineId).setState(State.Closed);
	}
	
	/*public Process startEngine() throws IOException {
		if (!engineStarted) {
			Process p = Runtime.getRuntime().exec(enginePath);
			//p.getOutputStream().write(("setoption name Max CPUs value "+cores+"\nsetoption name CPU Usage value 100\n").getBytes());
			p.getOutputStream().write(("setoption name Threads value "+cores+"\nsetoption name CPU Usage value 100\n").getBytes());
			engineStarted = true;
			return p;
		}
		return null;
	}
	
	public void close() {
		LOG.info("Closed Engine");
		connected = false;
		p.destroy();
		engineStarted = false;
	}*/
	
	public void close() {
		exit = true;
		//adminThread.interrupt();
		/*for(Thread t : serverThreads) {
			t.interrupt();
		}*/
		
		for (Entry<String, EngineServer> entry : servers.entrySet()) {
			entry.getValue().close();
		}
		/*if(adminServerSocket != null) {
			try {
				adminServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}
	
	/*public void handshake() throws IOException {
		adminNetworkRW = new TCPNetworkRW(adminServerSocket.accept());
		LOG.info("waiting for connection...");
		LOG.info("Connection received from " + adminNetworkRW.getAddress().getHostName());
		String request = null;
		String protocol = serverConfig.getProtocol();
		do {
			request = adminNetworkRW.readFromNetwork();
			LOG.fine("Client: " + request);
			String sentMsg = null;
			if (request.equalsIgnoreCase("helloserver")) {
				sentMsg = "connected";
				connected = true;
			} 
			else if (request.startsWith("protocol=")) {
				protocol = request.split("=")[1];
				if ("tcp".equalsIgnoreCase(protocol)) {
					if(!engineStarted)
						initEngines();
					sentMsg = "tcp protocol set";
				} else if ("udp".equalsIgnoreCase(protocol)) {
					sentMsg = "udp protocol set";
				} else {
					sentMsg = "unknown protocol set";
				}
			} else if ("exit".equals(request)) {
				adminNetworkRW.close();
				//exit = true;
			} else if ("close_engine".equals(request)) {
				p.destroy();
				sentMsg = "engine closed";
			} else {
				sentMsg = "unknown command";
			}
			if(null != sentMsg) {
				LOG.info("Server: " + sentMsg);
				adminNetworkRW.writeToNetwork(sentMsg);
			}
		} while (null != request && !"exit".equals(request));
	}*/
	
	public static String externalServerUrl = null;
	public static void main(String[] args) throws IOException, JAXBException {
		//System.getProperties().put("-Djava.util.logging.config.file", "h:/logging.properties");
		System.out.println(System.getProperty("java.home"));
		LOG.info("Admin Server : Log initialized");
		AdminServer server = new AdminServer("serverConfig.xml");
		externalServerUrl = server.getServerConfig().getExternalServerUrl();
		server.start();
		LOG.info("Admin Server closed");
	}
	
	private void initEngines() throws IOException {
		for(Server server : serverConfig.getServers()) {
			EngineServer engineServer = new EngineServer(server);
			servers.put(engineServer.getId(), engineServer);
			Thread t = new Thread(engineServer, server.getName());
			t.start();
			serverThreads.add(t);
			LOG.info("server listening: "+server);
		}
		engineStarted = true;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public Map<String, EngineServer> getServerDetails() {
		return servers;
	}
}
