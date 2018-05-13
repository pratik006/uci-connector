package com.prapps.chess.api.udp;

import static com.prapps.chess.api.udp.SharedContext.TIME_DIFF_ALLOWED;
import static com.prapps.chess.api.udp.State.*;
import java.util.Calendar;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;

public class GetOtherNatThread implements Runnable {
	private SharedContext ctx;
	
	public GetOtherNatThread(SharedContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void run() {
		while (!ctx.getExit().get()) {
			try {
				NatDetail otherNat = RestUtil.getOtherNatDetails(ctx.getBaseConfig().getExternalHost(), ctx.getId());
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
					Thread.sleep(TIME_DIFF_ALLOWED - Calendar.getInstance().getTimeInMillis() + otherNat.getLastUpdated());
				} else {
					System.out.println("Other computer is not online");
					Thread.sleep(5000);	
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
