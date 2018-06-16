package com.prapps.chess.server.uci.tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.logging.Logger;

import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.uci.share.AsyncReader;
import com.prapps.chess.uci.share.AsyncWriter;
import com.prapps.chess.uci.share.NetworkRW;
import com.prapps.chess.uci.share.TCPNetworkRW;

public class EngineServer extends Server implements Runnable {

	private static Logger LOG = Logger.getLogger(EngineServer.class.getName());
	
	private NetworkRW networkRW;
	public volatile boolean stateClosing = false;
	private ServerSocket serverSocket;
	private Thread guiToEngineWriterThread;
	private AsyncReader reader;
	private AsyncWriter writer;
	private Thread engineToGUIWriterThread;
	InputStream is;
	OutputStream os;
	
	public EngineServer(Server server) throws IOException {
		this.id = server.id;
		this.name = server.name;
		this.path = server.path;
		this.command = server.command;
		this.port = server.port;
		File file = new File(path);
		if(!file.exists()) {
			throw new FileNotFoundException("Invalid Engine Path"+path);
		}
		serverSocket = new ServerSocket(port);
		setState(State.New);
	}
	
	public void listen() throws IOException {		
		LOG.info(name+" -> TCP server port: "+port);
		LOG.info("waiting for connection on Engine port: "+serverSocket.getLocalPort());
		LOG.info("Engine: "+path);
		setState(State.Waiting);
		networkRW = new TCPNetworkRW(serverSocket.accept());
		setState(State.Connected);
		LOG.fine("\n--------------------------------------Start Server ----------------------------------\n");
		Process p = null;
		if(!networkRW.isClosed() && networkRW.isConnected()) {
			try {
				p = startEngine(path, command);
				os = p.getOutputStream();
				is = p.getInputStream();
				reader = new AsyncReader(os, networkRW, stateClosing);
				writer = new AsyncWriter(is, networkRW, stateClosing);
				guiToEngineWriterThread = new Thread(reader);
				engineToGUIWriterThread = new Thread(writer);
				// writer.setDaemon(true);
				guiToEngineWriterThread.start();
				engineToGUIWriterThread.start();
				setState(State.Running);

				if(guiToEngineWriterThread.isAlive())
					guiToEngineWriterThread.join();
				p.destroy();
				LOG.fine("Closing Engine on port "+networkRW.getPort());
				if(!networkRW.isConnected()) {
					networkRW.writeToNetwork("exit");
					networkRW.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				networkRW.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if(null != p)
					p.destroy();
				super.setState(State.Closed);
			}
		}
	}
	
	public Process startEngine(String enginePath, String command) throws IOException {
		Process p = null;
		if(null == command || "".equals(command)) {
			 p = Runtime.getRuntime().exec(enginePath);
		}
		else {
			p = new ProcessBuilder(command, enginePath).start();
		}
		return p;
	}

	public void run() {
		try {
			while(State.Exit != getState())
				listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setState(State.Closed);
		LOG.info(name + "Engine Server closed");
	}
	
	public void close() {
		setState(State.Closed);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(null  != guiToEngineWriterThread)
			guiToEngineWriterThread.interrupt();
		if(null != engineToGUIWriterThread)
			engineToGUIWriterThread.interrupt();
		if(null != guiToEngineWriterThread && null != engineToGUIWriterThread) {
			if(!engineToGUIWriterThread.isAlive() && !guiToEngineWriterThread.isAlive()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setState(State state) {
		if (State.Closed == state) {
			System.err.println("time to restart the server");
			try {
				networkRW.writeToNetwork("exit");
				reader.setState(State.Closed);
				writer.setState(State.Closed);
				is.close();
				os.write("quit\n".getBytes());
				os.flush();
				os.close();
				networkRW.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOG.finest(this.id + " state changed to : " + state);
		super.setState(state);
	}

}
