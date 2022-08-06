package org.apache.iotdb.ui.model;

public enum TaskFlag {

	LONG_TERM("l", "长期"), ONE_TIME("o", "一次性"), UNKNOW("u", "未知");

	private final String value;

	private final String msg;

	private TaskFlag(String value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public String getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

	public static TaskFlag forValue(String value) {
		if (value == null) {
			return UNKNOW;
		}
		switch (value) {
		case "l":
			return LONG_TERM;
		case "o":
			return ONE_TIME;
		default:
			return UNKNOW;
		}
	}
}
