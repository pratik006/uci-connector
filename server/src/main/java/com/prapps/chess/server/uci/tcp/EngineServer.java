package com.prapps.chess.server.uci.tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.prapps.chess.server.uci.thread.AsyncReader;
import com.prapps.chess.server.uci.thread.AsyncWriter;
import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.tcp.common.Engine;

public class EngineServer implements Runnable {

	private static Logger LOG = Logger.getLogger(EngineServer.class.getName());
	
	private Engine engine;
	private State state;
	public volatile boolean stateClosing = false;
	private ServerSocket serverSocket;
	private Thread guiToEngineWriterThread;
	private AsyncReader reader;
	private AsyncWriter writer;
	private Thread engineToGUIWriterThread;
	InputStream is;
	OutputStream os;
	
	public EngineServer(Engine engine) throws IOException {
		this.engine = engine;
		File file = new File(engine.getPath());
		if(!file.exists()) {
			throw new FileNotFoundException("Invalid Engine Path" + engine.getPath());
		}
		serverSocket = new ServerSocket(engine.getPort());
		setState(State.New);
	}
	
	public void listen() throws IOException {		
		LOG.info(engine.getId() + " -> TCP server port: " + engine.getPort());
		LOG.info("waiting for connection on Engine port: " + serverSocket.getLocalPort());
		LOG.info("Engine: "+engine.getPath());
		setState(State.Waiting);
		Socket sock = serverSocket.accept();
		LOG.info("connection received");
		setState(State.Connected);
		LOG.fine("\n--------------------------------------Start Server ----------------------------------\n");
		Process p = null;
		if(!sock.isClosed() && sock.isConnected()) {
			try {
				p = startEngine(engine.getPath(), engine.getCommand());
				os = p.getOutputStream();
				is = p.getInputStream();
				reader = new AsyncReader(os, sock.getInputStream(), stateClosing);
				writer = new AsyncWriter(is, sock.getOutputStream(), stateClosing);
				guiToEngineWriterThread = new Thread(reader);
				engineToGUIWriterThread = new Thread(writer);
				// writer.setDaemon(true);
				guiToEngineWriterThread.start();
				engineToGUIWriterThread.start();
				setState(State.Running);

				if(guiToEngineWriterThread.isAlive())
					guiToEngineWriterThread.join();
				p.destroy();
				LOG.fine("Closing Engine on port "+sock.getPort());
				if(!sock.isConnected()) {
					sock.getOutputStream().write("exit".getBytes());
					sock.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if(null != p)
					p.destroy();
				state = State.Closed;
				sock.close();
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
			while(State.Exit != state)
				listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setState(State.Closed);
		LOG.info(engine.getId() + "Engine Server closed");
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
			/*try {
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
			}*/
		}
		LOG.finest(engine.getId() + " state changed to : " + state);
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
	
	public String getId() {
		return engine.getId();
	}
	
	public int getPort() {
		return engine.getPort();
	}

}
