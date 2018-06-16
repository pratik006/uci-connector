package com.prapps.chess.server.uci.tcp;

import java.util.List;
public class ServerConfig {

    protected int adminPort;
    protected String externalServerUrl;
    protected List<Server> engines;
    protected String protocol;
    protected String password;
    protected String fromMail;
    protected String mailPass;
    protected String toMail;
    
	public int getAdminPort() {
		return adminPort;
	}
	public void setAdminPort(int adminPort) {
		this.adminPort = adminPort;
	}
	public String getExternalServerUrl() {
		return externalServerUrl;
	}
	public void setExternalServerUrl(String externalServerUrl) {
		this.externalServerUrl = externalServerUrl;
	}
	public List<Server> getEngines() {
		return engines;
	}
	public void setEngines(List<Server> engines) {
		this.engines = engines;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFromMail() {
		return fromMail;
	}
	public void setFromMail(String fromMail) {
		this.fromMail = fromMail;
	}
	public String getMailPass() {
		return mailPass;
	}
	public void setMailPass(String mailPass) {
		this.mailPass = mailPass;
	}
	public String getToMail() {
		return toMail;
	}
	public void setToMail(String toMail) {
		this.toMail = toMail;
	}

}
