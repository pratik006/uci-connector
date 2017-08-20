package com.prapps.chess.client.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RestUtil {
	public static final String POST_METHOD = "POST";
	public static final String GET_METHOD = "GET";
	
	public Map<String, String> makeRestCall(String strurl, String method, String token, String data) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		URL url = new URL(strurl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		if (token != null) {
			conn.setRequestProperty("Authorization", "Bearer "+token);
		}
		
		if (POST_METHOD.equals(method)) {
			conn.setDoOutput(true);	
			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder content = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			content.append(line);
		}
		map.put("content", content.toString());
		token = conn.getHeaderField("x-authtoken");
		map.put("x-authtoken", token);
		conn.disconnect();
		return map;
	}
	
	public String getToken(String url, String username, String pass) {
		String input = "{\"userName\": \""+username+"\", \"password\": \""+pass+"\"}";
		Map<String, String> map;
		try {
			map = makeRestCall(url, POST_METHOD, null, input);
			String token = map.get("x-authtoken");
			return token;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
