package org.apache.iotdb.ui.config.tsdatasource;

import org.apache.iotdb.session.SessionDataSet;

public class SessionDataSetWrapper {

	public SessionDataSetWrapper(Long timestamp_, SessionDataSet sessionDataSet_) {
		timestamp = timestamp_;
		sessionDataSet = sessionDataSet_;
	}

	private Long timestamp;

	private SessionDataSet sessionDataSet;

	private int times;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public SessionDataSet getSessionDataSet() {
		return sessionDataSet;
	}

	public void setSessionDataSet(SessionDataSet sessionDataSet) {
		this.sessionDataSet = sessionDataSet;
	}

	public int getTimes() {
		return times;
	}

	public int increaseTimes() {
		return ++times;
	}

}
