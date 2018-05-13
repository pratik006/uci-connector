package com.prapps.chess.nat.udp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.udp.DatagramListenerThread;
import com.prapps.chess.api.udp.GetOtherNatThread;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.State;
import com.prapps.chess.api.udp.StateChangeThread;
import com.prapps.chess.api.udp.StunMessageListener;
import com.prapps.chess.api.udp.StunMessageSender;
import com.prapps.chess.server.config.ServerConfig;

import de.javawi.jstun.header.MessageHeader;

public class UdpListener implements Runnable {
	private SharedContext ctx;
	
	public UdpListener(ServerConfig serverConfig) throws ClassNotFoundException, SocketException, UnknownHostException {
		ctx = new SharedContext();
		int srcPort = serverConfig.getUdpConfig().getSourcePort();
		DatagramSocket socket = new DatagramSocket(new InetSocketAddress(getLocalAddress(), srcPort));
		socket.setSoTimeout(serverConfig.getUdpConfig().getSocketTimeout());
		socket.setReuseAddress(true);
		ctx.setSocket(socket);
		ctx.setId(serverConfig.getServerId());
		ctx.setOtherId(serverConfig.getClientId());
		ctx.setBaseConfig(serverConfig);
		ctx.setExit(new AtomicBoolean(false));
		ctx.setNat(new AtomicReference<NatDetail>());
		ctx.setSendMHRef(new AtomicReference<MessageHeader>());
		ctx.setConnectionState(new AtomicReference<State>(new State()));
		ctx.setObjectMapper(new ObjectMapper());
		ctx.setListeners(Arrays.asList(
				new StunMessageListener(ctx),
				new ServerDatagramP2pListener(ctx, serverConfig, new AtomicLong())
			));
	}

	@Override
	public void run() {
		try {
			Thread stunSender = new Thread(new StunMessageSender(ctx));stunSender.start();
			Thread otherNatThread = new Thread(new GetOtherNatThread(ctx));otherNatThread.start();
			Thread datagramListener = new Thread(new DatagramListenerThread(ctx));datagramListener.start();
			Thread stateChangeThread = new Thread(new StateChangeThread(ctx));stateChangeThread.start();
			
			stunSender.join();
			otherNatThread.join();
			datagramListener.join();
			stateChangeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

}
