package org.apache.iotdb.ui.model;

public enum TaskType {

	EXPORT("e"), IMPORT("i"), UNKNOW("u");

	private final String value;

	private TaskType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
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
