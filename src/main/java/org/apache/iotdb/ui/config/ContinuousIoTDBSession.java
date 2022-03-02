package org.apache.iotdb.ui.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.ui.config.tsdatasource.SessionDataSetWrapper;
import org.apache.iotdb.ui.config.websocket.TimerConfig;

public class ContinuousIoTDBSession {

	public static Map<String, SessionDataSetWrapper> continuousDataSetWrapperMap = new ConcurrentHashMap<>();

	public static SessionDataSet getContinuousDataSet(String key) {
		SessionDataSetWrapper ret = continuousDataSetWrapperMap.get(key);
		return ret == null ? null : ret.getSessionDataSet();
	}

	public static SessionDataSetWrapper getContinuousDataSetWrapper(String key) {
		return continuousDataSetWrapperMap.get(key);
	}

	public static SessionDataSet addContinuousDataSet(String key, SessionDataSet sessionDataSet) {
		SessionDataSetWrapper sdsw = new SessionDataSetWrapper(TimerConfig.cou, sessionDataSet);
		SessionDataSetWrapper ret = continuousDataSetWrapperMap.put(key, sdsw);
		return ret == null ? null : ret.getSessionDataSet();
	}

	public static SessionDataSet removeContinuousDataSet(String key) {
		SessionDataSetWrapper ret = continuousDataSetWrapperMap.remove(key);
		return ret == null ? null : ret.getSessionDataSet();
	}
}
