package com.prapps.chess.client.tcp.ui;

import java.io.IOException;

import com.prapps.chess.uci.share.NetworkRW;

public class ThreadRW {
	
	public void write(final NetworkRW networkRW, final String msg) {
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					networkRW.writeToNetwork(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void read(final NetworkRW networkRW, final StringBuilder sb) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					String msg = networkRW.readFromNetwork();
					sb.append(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
