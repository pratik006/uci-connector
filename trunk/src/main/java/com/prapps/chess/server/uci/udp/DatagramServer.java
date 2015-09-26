package com.prapps.chess.server.uci.udp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import com.prapps.chess.uci.share.ProtocolConstants;
import com.prapps.chess.uci.share.UCIUtil;

public class DatagramServer implements Runnable {

	private static Logger LOG = Logger.getLogger(DatagramServer.class.getName());

	public static final int DEFAULT_PORT = 11000;
	private DatagramSocket datagramSocket = null;
	private static Properties config;
	public Boolean exit = false;
	private String enginePath;
	private boolean started = false;
	private int cores = -1;
	private static int adminPort;

	private static List<DatagramServer> servers = new ArrayList();
	private byte[] password;

	public DatagramServer(String enginePath, int port) throws FileNotFoundException, IOException, MessagingException {
		
		
		InetAddress.getByName(config.getProperty("client_ip"));
		datagramSocket = new DatagramSocket(port);
		// datagramSocket.setReuseAddress(true);
		// datagramSocket.bind(new InetSocketAddress("192.168.1.2", 11000));
		System.out.println(datagramSocket.getLocalSocketAddress());
		System.out.println(datagramSocket.getLocalPort());
		this.enginePath = enginePath;
		password = config.getProperty("secretKey").getBytes();
		cores = Runtime.getRuntime().availableProcessors();
		LOG.info("available cores: "+cores);
		if(cores > 1)
			cores = cores-1;
		LOG.info("usable cores: "+cores);
		System.out.println("cores: "+cores);
		// LOG.info("Port number: "+DEFAULT_PORT+" "+datagramSocket.getLocalSocketAddress());
	}

	public DatagramServer(int port) throws FileNotFoundException, IOException, MessagingException {
		enginePath = config.getProperty(String.valueOf(port));
		datagramSocket = new DatagramSocket(adminPort+port);		
		System.out.println("======="+datagramSocket.getLocalAddress());
		password = config.getProperty("secretKey").getBytes();
		System.out.println("listening on port: " + datagramSocket.getLocalPort() + "\tengine: " + enginePath);
		
		cores = Runtime.getRuntime().availableProcessors();
		LOG.info("available cores: "+cores);
		if(cores > 1)
			cores = cores-1;
		LOG.info("usable cores: "+cores);
		System.out.println("cores: "+cores);
	}

	public void listen() {
		byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		Process p = null;
		boolean init = false;
		String sentMsg = null;
		String receivedText = null;

		try {
			while (!exit) {
				if (init) {
					if (!started) {
						p = Runtime.getRuntime().exec(enginePath);
						//p.getOutputStream().write(("setoption name Max CPUs value "+cores+"\nsetoption name CPU Usage value 100\n").getBytes());
						p.getOutputStream().write(("setoption name Threads value "+cores+"\nsetoption name CPU Usage value 100\n").getBytes());
						started = true;
						LOG.fine("Connection received from " + packet.getAddress().getHostName());
					}

					Thread reader = new Thread(new AsyncDatagramReader(p.getOutputStream(), datagramSocket, packet, exit, password));
					Thread writer = new Thread(new AsyncDatagramWriter(p.getInputStream(), datagramSocket, packet.getAddress(), packet.getPort(),
							exit, password));
					// writer.setDaemon(true);
					reader.start();
					writer.start();

					if (reader.isAlive())
						reader.join();
					if (writer.isAlive())
						writer.join();

					LOG.info("Closing Engine");
					/*
					 * newPacket = new DatagramPacket("exit".getBytes(), 4,
					 * packet.getAddress(), packet.getPort());
					 * datagramSocket.send(newPacket);
					 */
					UCIUtil.sendPacket("exit".getBytes(UCIUtil.CHARSET), password, datagramSocket, packet.getAddress(), packet.getPort());
					LOG.info("Closed Engine");
					init = false;
					p.destroy();
					started = false;
				} else {
					packet = new DatagramPacket(buffer, buffer.length);
					LOG.info("waiting for packet... on port " + datagramSocket.getLocalPort());
					datagramSocket.receive(packet);
					receivedText = UCIUtil.readPacket(packet, password);
					LOG.info("packet recvd " + receivedText + " from " + packet.getSocketAddress());
					if (receivedText.equalsIgnoreCase("helloserver")) {
						sentMsg = "connected";
						init = true;
					} else if ("exit".equals(receivedText)) {
						exit = true;
					} else if ("close_engine".equals(receivedText)) {
						p.destroy();
						sentMsg = "engine closed";
					} else {
						sentMsg = "unknown command";
					}
					LOG.info("packet sent: " + sentMsg);
					UCIUtil.sendPacket(sentMsg.getBytes(UCIUtil.CHARSET), password, datagramSocket, packet.getAddress(), packet.getPort());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			p.destroy();
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, MessagingException {
		System.getProperties().put("Djava.util.logging.config.file", "logging.properties");
		LOG.info("server starting");
		config = new Properties();
		config.load(new FileInputStream("config.ini"));
		adminPort = UCIUtil.getAdminPort();
		try {
			LOG.info(UCIUtil.getExternalIP());
			UCIUtil.mailExternalIP(UCIUtil.getExternalIP()+":admin_port="+adminPort, config.getProperty("fromMail"), config.getProperty("mailPass"), config.getProperty("toMail"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		int count = 0;
		for (int port = 0; port <= 10; port++) {
			if (config.containsKey(String.valueOf(port))) {
				servers.add(new DatagramServer(port));				
				new Thread(servers.get(count)).start();
				count++;
			}
		}
		
		new Thread(new Runnable() {
			
			public void run() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						if(line.contains(":q")) {
							for(DatagramServer s : servers) {
								s.exit = true;
							}
							break;
						}
							
					}
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}).start();
		LOG.info("server started");
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}
	
	public void run() {
		listen();
	}
}
