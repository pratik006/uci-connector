package com.prapps.chess.server.uci.tcp.thread;

import java.util.logging.Logger;

import com.prapps.chess.server.uci.tcp.ServerConfig;
import com.prapps.chess.server.uci.thread.AbstractRunnable;
import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.uci.share.UCIUtil;

public class MailerThread extends AbstractRunnable {

	private static final Logger LOG = Logger.getLogger(MailerThread.class.getName());

	private ServerConfig serverConfig;

	public MailerThread(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
		setState(State.New);
	}

	
	public void run() {
		try {
			setState(State.Running);
			String exIp = UCIUtil.getExternalIP();
			/*UCIUtil.mailExternalIP(UCIUtil.getExternalIP() + ":admin_port=" + serverConfig.getAdminPort(), serverConfig.getFromMail(),
					serverConfig.getMailPass(), serverConfig.getToMail());*/
			while(getState() == State.Running) {
				UCIUtil.updateExternalIP(exIp, serverConfig.getAdminPort());
				LOG.info(UCIUtil.getExternalIP());
				Thread.sleep(360000);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
