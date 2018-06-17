package com.prapps.chess.tcp.common;

public class Engine {
	private int port;
	private int offsetPort;
	private String id;
	private boolean enabled;
	private String path;
	private String command;
	
	public Engine() { }
	
	public Engine(String id, int offsetPort) {
		this.id = id;
		this.offsetPort = offsetPort;
	}
	
	public int getOffsetPort() {
		return offsetPort;
	}
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setOffsetPort(int offsetPort) {
		this.offsetPort = offsetPort;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
