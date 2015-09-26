package com.prapps.chess.server.uci.tcp;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ServerConfigUtil {

	private ServerConfig serverConfig;
	
	public ServerConfigUtil(InputStream is) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(ServerConfig.class);
		Unmarshaller unmarshaller = ctx.createUnmarshaller();
		serverConfig = (ServerConfig) unmarshaller.unmarshal(is);
	}
	
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
}
