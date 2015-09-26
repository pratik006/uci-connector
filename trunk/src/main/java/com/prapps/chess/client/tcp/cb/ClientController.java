package com.prapps.chess.client.tcp.cb;

import java.io.IOException;
import java.util.logging.Logger;

import com.prapps.chess.client.tcp.ui.TCPClientUtil;
import com.prapps.chess.uci.share.ProtocolConstants;

public class ClientController {

	private static final Logger LOG = Logger.getLogger(ClientController.class.getName());
	private final TCPClientUtil tcpClientUtil;
	private ServerConnector serverConnector;
	
	public ClientController(TCPClientUtil tcpClientUtil) {
		this.tcpClientUtil = tcpClientUtil;
		try {
			serverConnector = ServerConnector.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			serverConnector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ServerDetails getAvailableServers() throws IOException {
		String servers = serverConnector.sendMsg(ProtocolConstants.GET_AVAILABLE_ENGINES);
		ServerDetails serverDetails = new ServerDetails(servers);
		LOG.finest("Server: " + serverDetails);
		return serverDetails;
	}
	
	public void retartServer(final String serverId) throws IOException {
		LOG.info("restarting server : "+serverId);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					String resp = serverConnector.sendMsg(ProtocolConstants.RESTART_ENGINE+"="+serverId);
					LOG.fine("Server : "+resp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	public void shutDown() throws IOException {
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					String msg = serverConnector.sendMsg(ProtocolConstants.SHUT_DOWN);
					LOG.fine("Server : "+msg);
					if (ProtocolConstants.CLOSE_MSG.equals(msg)) {
						//(DefaultTableModel) tblServerDetails.getModel()).setRowCount(0);
						tcpClientUtil.clearTable();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
}
