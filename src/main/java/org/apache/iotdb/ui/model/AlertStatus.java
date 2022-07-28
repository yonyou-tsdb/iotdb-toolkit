package org.apache.iotdb.ui.model;

public enum AlertStatus {

	DEVELOP("0", "开发中"), DEPLOYED("1", "已部署"), UNKNOW("9", "未知");

	private final String value;

	private final String msg;

	private AlertStatus(String value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public String getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

	public static AlertStatus forValue(String value) {
		if (value == null) {
			return UNKNOW;
		}
		switch (value) {
		case "0":
			return DEVELOP;
		case "1":
			return DEPLOYED;
		default:
			return UNKNOW;
		}
	}
}
