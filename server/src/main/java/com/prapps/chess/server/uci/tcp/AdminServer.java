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

import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.tcp.common.Engine;
import com.prapps.chess.tcp.common.RestUtil;

public class AdminServer {

	private static Logger LOG = Logger.getLogger(AdminServer.class.getName());
	
	private ServerConfigDetail serverConfig;
	protected int cores = -1;
	protected static int adminPort;
	public boolean exit;
	protected boolean engineStarted = false;
	protected boolean connected = false;
	protected Process p;
	//protected ServerSocket adminServerSocket;

	protected String enginePath;
	protected char[] password;
	protected static Map<String, EngineServer> servers = new HashMap<String, EngineServer>();
	protected static List<Thread> serverThreads = new ArrayList<Thread>();
	
	protected Thread guiToEngineWriterThread;
	protected Thread engineToGUIWriterThread;
	private Thread adminThread;
	
	public AdminServer(String configPath) throws IOException {
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

	public void start() throws IOException, ClassNotFoundException {
		//AdminServer.config = config;
		LOG.info("server starting");
		adminPort = serverConfig.getPort();
		//protocol = config.getProperty("protocol");
		LOG.info("admin port: "+adminPort);
		//adminServerSocket = new ServerSocket(adminPort);
		RestUtil.udpateServerConfig(serverConfig);
		
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
		
		initEngines();
		LOG.fine("Admin server closed");
	}
	
	public void restartEngine(String engineId) {
		LOG.info("Restart Engine: "+engineId);
		servers.get(engineId).setState(State.Closed);
	}
	
	public void close() {
		exit = true;
		for (Entry<String, EngineServer> entry : servers.entrySet()) {
			entry.getValue().close();
		}
	}
	
	public static String externalServerUrl = null;
	public static void main(String[] args) throws Exception {
		//System.getProperties().put("-Djava.util.logging.config.file", "h:/logging.properties");
		//System.out.println(System.getProperty("java.home"));
		LOG.info("Admin Server : Log initialized");
		AdminServer server = new AdminServer("serverConfig.json");
		externalServerUrl = server.getServerConfig().getExternalServerUrl();
		server.start();
		LOG.info("Admin Server closed");
	}
	
	private void initEngines() throws IOException {
		for(Engine engine : serverConfig.getEngines()) {
			EngineServer engineServer = new EngineServer(engine);
			servers.put(engine.getId(), engineServer);
			Thread t = new Thread(engineServer, engine.getId());
			t.start();
			serverThreads.add(t);
			LOG.info("server listening: "+engine);
		}
		engineStarted = true;
	}

	public ServerConfigDetail getServerConfig() {
		return serverConfig;
	}
	
	public Map<String, EngineServer> getServerDetails() {
		return servers;
	}
}
