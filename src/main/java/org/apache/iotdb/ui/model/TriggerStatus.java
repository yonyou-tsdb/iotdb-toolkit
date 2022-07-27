package org.apache.iotdb.ui.model;

public enum TriggerStatus {

	DELETE("0", "删除"), ENABLE("1", "启用"), DISABLE("2", "禁用"), UNKNOW("9", "未知");

	private final String value;

	private final String msg;

	private TriggerStatus(String value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public String getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

	public static TriggerStatus forValue(String value) {
		if (value == null) {
			return UNKNOW;
		}
		switch (value) {
		case "0":
			return DELETE;
		case "1":
			return ENABLE;
		case "2":
			return DISABLE;
		default:
			return UNKNOW;
		}
	}
}
