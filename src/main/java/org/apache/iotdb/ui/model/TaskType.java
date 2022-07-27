package org.apache.iotdb.ui.model;

public enum TaskType {

	EXPORT("e", "备份"), IMPORT("i", "恢复"), UNKNOW("u", "未知");

	private final String value;

	private final String msg;

	private TaskType(String value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public String getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

	public static TaskType forValue(String value) {
		if (value == null) {
			return UNKNOW;
		}
		switch (value) {
		case "e":
			return EXPORT;
		case "i":
			return IMPORT;
		default:
			return UNKNOW;
		}
	}
}
