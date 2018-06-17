package com.prapps.chess.tcp.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestUtil {

	public static void udpateServerConfig(ServerConfig config) throws IOException, ClassNotFoundException {
		StringWriter writer = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode node = mapper.readTree(getPublicIp());
		config.setIp(node.get("ip").asText());
		config.setLocalIp(getLocalAddress().getHostAddress());
		
		mapper.writeValue(writer, config);
		updateServerConfig(writer.toString(), config.getId());
	}
	
	public static InetAddress getLocalAddress() throws ClassNotFoundException, SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
			while (iaddresses.hasMoreElements() && iface.getDisplayName().indexOf("docker") == -1) {
				InetAddress iaddress = iaddresses.nextElement();
				if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
					if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
						return iaddress;
					}
				}
			}
		}
		
		return null;
	}
	
	public static String getPublicIp() throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL("https://api.ipify.org?format=json").openConnection();
		byte[] buf = new byte[1024];
		int read = -1;
		StringBuilder sb = new StringBuilder();
		while ((read = conn.getInputStream().read(buf)) != -1 ) {
			sb.append(new String(buf, 0, read));
		}
		return sb.toString();
	}
	
	public static void updateServerConfig(String json, String id) throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL("https://uci-connector.firebaseio.com/servers/"+ id +".json").openConnection();
		conn.setRequestMethod("PUT");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		conn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(json);
		wr.flush();
		wr.close();
		
		int responseCode = conn.getResponseCode();
		if (responseCode != 200 && responseCode != 201) {
			throw new IOException("failed updating server config");
		}
	}
}
