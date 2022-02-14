package org.apache.iotdb.ui.config.tsdatasource;

import org.apache.iotdb.session.pool.SessionPool;

public class SessionPoolWrapper {

	public SessionPoolWrapper(String host_, int port_, String username_, String password_, int maxSize) {
		host = host_;
		port = port_;
		username = username_;
		password = password_;
		sessionPool = new SessionPool(host_, port_, username_, password_, maxSize);
	}

	private String host;

	private int port;

	private String username;

	private String password;

	private SessionPool sessionPool;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public SessionPool getSessionPool() {
		return sessionPool;
	}

	public void setSessionPool(SessionPool sessionPool) {
		this.sessionPool = sessionPool;
	}

}
