package com.prapps.chess.api.udp.thread;

import static com.prapps.chess.api.udp.SharedContext.TIME_DIFF_ALLOWED;
import static com.prapps.chess.api.udp.State.RECEIVED_OTHER_MAC;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;
import com.prapps.chess.api.udp.SharedContext;
import com.prapps.chess.api.udp.State;

public class GetOtherNatThread implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(GetOtherNatThread.class);
	private SharedContext ctx;
	
	public GetOtherNatThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		while (!ctx.getExit().get()) {
			while (ctx.getConnectionState().get().getState() != State.MAC_UPDATED) {
				try {
					synchronized (ctx.getConnectionState()) {
						ctx.getConnectionState().wait();	
					}
				} catch (InterruptedException e) { if (!ctx.getExit().get()) e.printStackTrace(); }
			}
			
			try {
				NatDetail otherNat = RestUtil.getOtherNatDetails(ctx.getBaseConfig().getExternalHost(), ctx.getOtherId());
				if (Calendar.getInstance().getTimeInMillis() - otherNat.getLastUpdated() < TIME_DIFF_ALLOWED) {
					synchronized (ctx.getNat()) {
						ctx.getNat().set(otherNat);
						ctx.getNat().notifyAll();	
					}
					if (ctx.getConnectionState().get().isHigherState(RECEIVED_OTHER_MAC)) {
						synchronized (ctx.getConnectionState()) {
							ctx.getConnectionState().get().setState(RECEIVED_OTHER_MAC);
							ctx.getConnectionState().notifyAll();
						}	
					}
					
					if (ctx.getBaseConfig().isClient()) {
						try {
							ctx.getBaseConfig().getUdpConfig().setNat(otherNat);
							ctx.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
							ctx.getObjectMapper().writeValue(new File("clientConfig.json"), ctx.getBaseConfig());
							LOG.debug("clientConfig updated");
						} catch (IOException e) {
							e.printStackTrace();
						}	
					}
					Thread.sleep(TIME_DIFF_ALLOWED - Calendar.getInstance().getTimeInMillis() + otherNat.getLastUpdated());
				} else {
					System.out.println("Other computer is not online");
					Thread.sleep(5000);	
				}
			} catch (InterruptedException e) { 
				if (!ctx.getExit().get()) { 
					LOG.error(e.getMessage());
				}
			}
		}
		LOG.info("exitting GetOtherNatThread");
	}
}
