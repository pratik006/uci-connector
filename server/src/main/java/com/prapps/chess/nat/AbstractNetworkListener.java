package com.prapps.chess.nat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.Message;
import com.prapps.chess.server.EngineController;
import com.prapps.chess.server.config.ServerConfig;

public abstract class AbstractNetworkListener implements Runnable {
	protected LinkedList<Message> output = new LinkedList<>();
	protected AtomicBoolean exit;
	protected ServerConfig serverConfig;
	protected ObjectMapper mapper = new ObjectMapper();
	protected EngineController engineController;
	protected Set<String> engineIds = new HashSet<>();
	
	public AbstractNetworkListener(AtomicBoolean exit, ServerConfig serverConfig) {
		this.exit  = exit;
		this.serverConfig = serverConfig;
		this.engineController = EngineController.INSTANCE;
		this.engineController.setOutput(output);
		this.serverConfig.getServers().stream().forEach(server -> engineIds.add(server.getId()));
		listenEngine();
	}
	
	public void listenEngine() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!exit.get()) {
					while (!exit.get() && output.isEmpty()) {
						synchronized (output) {
							try {
								output.wait(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					send(output.poll());
				}
			}
		}).start();
	}
	
	public static InetAddress getLocalAddress() throws ClassNotFoundException, SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
			while (iaddresses.hasMoreElements() && iface.getDisplayName().indexOf("docker") == -1) {
				InetAddress iaddress = iaddresses.nextElement();
				if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
					if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
						return iaddress;
					}
				}
			}
		}
		
		return null;
	}
	
	public void handleMessage(Message msg) throws IOException {
		if (engineIds.contains(msg.getEngineId()))
			engineController.addMessage(msg);
		else {
			String str = new String(msg.getData());
			if (str.indexOf("get_available_engines") != -1) {
				send(new Message(1, null, Arrays.toString(engineIds.toArray())));
			}
		}
	}
	
	protected abstract void send(Message msg);
}