package com.prapps.chess.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.prapps.chess.api.Message;
import com.prapps.chess.server.config.ServerLoader;

public enum EngineController {
	INSTANCE;
	
	private LinkedList<Message> input = new LinkedList<>();
	private LinkedList<Message> output;
	
	private Map<String, Engine> engineMap = new HashMap<>();
	private Map<String, Thread> engineThreads = new HashMap<>();
	
	EngineController() {
		ServerLoader.INSTANCE.getServers().forEach(server -> engineMap.put(server.getId(), server));
	}
	
	public void setOutput(LinkedList<Message> output) {
		this.output = output;
	}
	
	public void addMessage(Message msg) throws IOException {
		Engine engine = engineMap.get(msg.getEngineId());
		if (engine != null) {
			if (!engine.isStarted())
				startEngine(msg.getEngineId());
			
			engine.write((new String(msg.getData())+"\n").getBytes());
		}
	}
	
	public void startEngine(final String id) throws IOException {
		Engine engine = engineMap.get(id);
		if (engine != null && !engine.isStarted()) {
			engine.start();
			Thread t = new Thread(new Runnable() {
				private String engineId = id;
				@Override
				public void run() {
					try {
						while (engine.isStarted()) {
							byte[] buf = new byte[1024];
							int readLen = -1;
							while ((readLen = engine.getInputStream().read(buf)) != -1) {
								byte[] data = new String(buf, 0, readLen).getBytes();
								//System.out.println(new String(data));
								output.add(new Message(1, engineId, data));
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("exiting thread "+Thread.currentThread().getName());
				}
			}, id);
			engineThreads.put(id, t);
			t.start();
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("-===============================");
		LinkedList<Message> output = new LinkedList<>();
		EngineController instance = EngineController.INSTANCE;
		instance.setOutput(output);
		AtomicBoolean exit = new AtomicBoolean(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!exit.get()) {
					while (!exit.get() && output.isEmpty()) {
						synchronized (output) {
							try {
								output.wait(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					System.out.println(new String(output.poll().getData()));
				}
				System.out.println("exiting reader thread");
			}
		}).start();
		EngineController.INSTANCE.addMessage(new Message(1, "critter", "uci\n".getBytes()));
		EngineController.INSTANCE.addMessage(new Message(1, "critter", "isready\n".getBytes()));
		EngineController.INSTANCE.addMessage(new Message(1, "stockfish", "uci\n".getBytes()));
		EngineController.INSTANCE.addMessage(new Message(1, "stockfish", "isready\n".getBytes()));
		Thread.sleep(2000);
		exit.set(true);
		EngineController.INSTANCE.shutdown();
	}
	
	public void shutdown() {
		engineMap.values().forEach(engine -> {
			if (engine.isStarted()) {
				try {
					engine.stop();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		});
	}

}
