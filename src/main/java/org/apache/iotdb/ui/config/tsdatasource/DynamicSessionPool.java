package org.apache.iotdb.ui.config.tsdatasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.pool.SessionPool;

public class DynamicSessionPool {

	private String defaultSessionPoolName;

	private SessionPoolWrapper defaultSessionPoolWrapper;

	private Map<Object, SessionPoolWrapper> sessionPoolMap = new ConcurrentHashMap<>(8);

	private Map<Object, SessionPoolWrapper> getSessionPoolMap() {
		return sessionPoolMap;
	}

	public SessionPoolWrapper getDefaultSessionPoolWrapper() {
		return defaultSessionPoolWrapper;
	}

	public String getDefaultSessionPoolName() {
		return defaultSessionPoolName;
	}

	public void registerDefaultSessionPool(String name, String host, int port, String username, String password,
			int maxSize) {
		defaultSessionPoolName = name;
		defaultSessionPoolWrapper = new SessionPoolWrapper(host, port, username, password, maxSize);
	}

	public Session determineTemporarySession() {
		SessionPoolWrapper spw = determineTargetSessionPoolWrapper();
		return new Session(spw.getHost(), spw.getPort(), spw.getUsername(), spw.getPassword());
	}

	public SessionPool determineTargetSessionPool() {
		return determineTargetSessionPoolWrapper().getSessionPool();
	}

	public SessionPoolWrapper determineTargetSessionPoolWrapper() {
		Object key = determineCurrentLookupKey();
		if (key.equals(getDefaultSessionPoolName())) {
			return getDefaultSessionPoolWrapper();
		} else {
			SessionPoolWrapper ret = getSessionPoolMap().get(key);
			if (ret == null) {
				throw new RuntimeException("Cannot find session pool.");
			}
			return ret;
		}
	}

	protected Object determineCurrentLookupKey() {
		return DynamicTSDataSourceContextHolder.getSessionPoolKey();
	}

	public SessionPool getSessionPool(Object name) {
		return sessionPoolMap.get(name).getSessionPool();
	}

	public void addSessionPool(Object name, String host, int port, String username, String password, int maxSize) {
		SessionPoolWrapper spw = new SessionPoolWrapper(host, port, username, password, maxSize);
		sessionPoolMap.put(name, spw);
	}

	public void removeSessionPool(Object name) {
		sessionPoolMap.remove(name);
	}
}
