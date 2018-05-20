package com.prapps.chess.api.udp;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prapps.chess.api.config.BaseConfig;

import de.javawi.jstun.header.MessageHeader;

public abstract class AbstractUdpBase extends AbstractNetworkBase {
	protected Logger LOG = LoggerFactory.getLogger(AbstractUdpBase.class);
	protected SharedContext ctx;
	
	public AbstractUdpBase(BaseConfig config) throws Exception {
		DatagramSocket socket = new DatagramSocket(config.getUdpConfig().getSourcePort(), getLocalAddress());
		//socket.setReuseAddress(true);
		//socket.setSoTimeout(config.getUdpConfig().getSocketTimeout());
		//updateMacAddress(socket, config.getClientId());
		ctx = new SharedContext();
		ctx.setSocket(socket);
		ctx.setId(config.getId());
		ctx.setOtherId(config.getOtherId());
		ctx.setBaseConfig(config);
		ctx.setExit(new AtomicBoolean(false));
		ctx.setNat(new AtomicReference<>());
		ctx.setSendMHRef(new AtomicReference<MessageHeader>());
		ctx.setConnectionState(new AtomicReference<State>(new State()));
		ctx.setObjectMapper(new ObjectMapper());
		ctx.setListeners(new ArrayList<>(2));
		ctx.getListeners().add(new StunMessageListener(ctx));
		ctx.getListeners().add(new DatagramHandshakeListener(ctx));
	}
}
