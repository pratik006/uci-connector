package com.prapps.chess.client.http;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;

public class HttpChessClient {

	private RestUtil restUtil = new RestUtil();
	private boolean stopFlag;
	private Properties config;
	private String baseUrl;
	private String loginUri;
	private String appUsername;
	private String appPassword;
	private String clientUri;
	private int refreshInterval;
	
	public HttpChessClient(Properties properties) {
		this.config = properties;
		baseUrl = config.getProperty("app-base-url");
		loginUri = config.getProperty("app-login-uri");
		appUsername = config.getProperty("app-username");
		appPassword = config.getProperty("app-password");
		clientUri = config.getProperty("app-client-uri");
		refreshInterval = Integer.parseInt(config.getProperty("refresh-interval"));
	}
	
	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.load(new FileInputStream("client-config.ini"));
		HttpChessClient client = new HttpChessClient(prop);
		client.start();
	}

	public void start() throws Exception {
		final String token = restUtil.getToken(baseUrl+loginUri, appUsername, appPassword);
		final Gson gson = new Gson();
		new Thread(new Runnable() {
			public void run() {
				while (!stopFlag) {
					try {
						StringBuilder sb = new StringBuilder();
						
						byte[] buf = new byte[1024*10];
						int read = -1;
						while (System.in.available() > 0 && (read = System.in.read(buf)) != -1) {
							sb.append(new String(buf, 0, read));
						}
						
						Message msg = new Message();
						msg.setMsg(sb.toString());
						Map<String, String> map = restUtil.makeRestCall(baseUrl+clientUri, RestUtil.POST_METHOD, token, gson.toJson(msg));
						String content = map.get("content");
						if (content != null && !"empty".equalsIgnoreCase(content)) {
							System.out.println(content);
						}
						
						if (sb.toString() != null && sb.toString().length()>0 && sb.toString().contains("quit")) {
							stopFlag = true;
						}
						Thread.sleep(refreshInterval);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
}
