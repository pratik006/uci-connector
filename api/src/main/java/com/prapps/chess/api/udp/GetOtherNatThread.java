package com.prapps.chess.api.udp;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.prapps.chess.api.NatDetail;
import com.prapps.chess.api.RestUtil;

public class GetOtherNatThread implements Runnable {
	private static int TIME_DIFF_ALLOWED = 5*60*1000;
	
	private AtomicBoolean exit;
	private String externalHost;
	private String otherId;
	private AtomicReference<NatDetail> natRef;
	
	public GetOtherNatThread(AtomicBoolean exit, AtomicReference<NatDetail> natRef, String externalHost, String otherId) {
		this.exit = exit;
		this.natRef = natRef;
		this.externalHost = externalHost;
		this.otherId = otherId;
	}
	
	@Override
	public void run() {
		while (!exit.get()) {
			try {
				NatDetail otherNat = RestUtil.getOtherNatDetails(externalHost, otherId);
				if (Calendar.getInstance().getTimeInMillis() - otherNat.getLastUpdated() < TIME_DIFF_ALLOWED) {
					natRef.set(otherNat);
					synchronized (natRef) {
						natRef.notifyAll();	
					}
					System.out.println(natRef.get());
					Thread.sleep(TIME_DIFF_ALLOWED - Calendar.getInstance().getTimeInMillis() + otherNat.getLastUpdated());
				} else {
					System.out.println("Other computer is not online");
					Thread.sleep(1000);	
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
