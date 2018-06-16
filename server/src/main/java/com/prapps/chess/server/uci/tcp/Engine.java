package com.prapps.chess.server.uci.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;


public class Engine {
	
	public static final boolean STOPED = false;
	public static final boolean STARTED = true;
	public static final String ATTRIBUTE_SEPERATOR = ",";
	public static final String ENGINE_STOP_COMMAND = "quit"+LineSeparator.Windows;
	
	private String name;
	private String engineId;
	private String path;
	private int port;
	private Process process;
	private boolean state = STOPED;
	
	public Engine(String path) throws IOException {
		if(null != path && !path.isEmpty()) {
			if(path.contains(ATTRIBUTE_SEPERATOR)) {
				toEngine(path);
			}
			else {
				this.path = path;
				if(!selfTest())
					throw new IOException("cannot create Engine");
			}
		}
		else {
			throw new IOException("Path given is empty or blank");
		}
	}
	
	public boolean selfTest() {
		byte[] buffer = new byte[1024*10];
        int readLen = buffer.length;
        StringBuilder sb = new StringBuilder();
        String idName;
        try {
            start();
            process.getOutputStream().write(("uci\r\n"+LineSeparator.Windows).getBytes());
            process.getOutputStream().flush();
            Thread.sleep(500);
            readLen = buffer.length;
            while(readLen >= buffer.length && (readLen = process.getInputStream().read(buffer)) > 0) {
                sb.append(new String(buffer, 0, readLen));
            }
            //System.out.println(sb);
            process.getOutputStream().write((ENGINE_STOP_COMMAND).getBytes());
            process.getOutputStream().flush();
            if(sb.toString().indexOf("id name") != -1) {
                idName = sb.toString().substring(sb.toString().indexOf("id name")+"id name".length(), sb.toString().length()-1);
                idName = idName.substring(0, idName.indexOf("\n"));   
                engineId = idName.trim();
                setName(engineId);
                engineId = engineId.replaceAll(" ", "_");
            }
            if(null != engineId && !engineId.isEmpty()) {
            	return true;
            }
            
        } catch (IOException ex) {
        	ex.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
        finally {
            try {
                stop();
            } catch (IOException ex) {
                process.destroy();
            }
        }
		return false;
	}
	
	public void changeState(boolean state) throws IOException {
		if(state==STARTED)
			start();
		else 
			stop();
	}
	
	public void start() throws IOException {
		if(state != STARTED) {
			System.out.println("Starting engine at "+path);
			process = Runtime.getRuntime().exec(path);
			state = STARTED;
		}
	}
	
	public void stop() throws IOException {
		if(state == STARTED) {
			process.getOutputStream().write(ENGINE_STOP_COMMAND.getBytes());
			process.getOutputStream().flush();
			process.destroy();
			state = STOPED;
			System.out.println("process stopped successfully !");
		}	
		else {
			System.out.println("process was not started");
		}
	}
	
	public InputStream getInputStream() {
		return process.getInputStream();
	}
	
	public OutputStream getOutputStream() {
		return process.getOutputStream();
	}
	
	public Engine(String name, String path) throws IOException {
		this(path);
		this.name = name;		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean isState() {
		return state;
	}

	public String toString() {
		if(null != path && !path.isEmpty()) {
			return engineId+ATTRIBUTE_SEPERATOR+name+ATTRIBUTE_SEPERATOR+path.replaceAll("\\\\", "/")+ATTRIBUTE_SEPERATOR+port;
		}
		else {
			return null;
		}
	}
	
	public void toEngine(String line) throws IOException {
		System.out.println(line);
		String attributes[] = line.split(ATTRIBUTE_SEPERATOR);
		if(attributes.length < 4)
			throw new IOException("Non Parsable Line Exception, attribute count is less than 4 while expected is 4");
		engineId = attributes[0];
		name = attributes[1];
		path = attributes[2];
		try {
			port = Integer.parseInt(attributes[3]);
		}
		catch(NumberFormatException ex) {
			throw new IOException("Port number:"+attributes[3]+" is not parsable");
		}
	}
}
