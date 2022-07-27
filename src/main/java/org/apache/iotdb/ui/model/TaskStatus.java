package org.apache.iotdb.ui.model;

public enum TaskStatus {

	NOT_START("0", "未开始"), IN_PROGRESS("1", "进行中"), NORMAL_END("2", "正常结束"), ABEND("3", "异常结束"),
	FORCED_END("4", "强制结束"), UNKNOW("9", "未知");

	private final String value;

	private final String msg;

	private TaskStatus(String value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public String getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

	public static TaskStatus forValue(String value) {
		if (value == null) {
			return UNKNOW;
		}
		switch (value) {
		case "0":
			return NOT_START;
		case "1":
			return IN_PROGRESS;
		case "2":
			return NORMAL_END;
		case "3":
			return ABEND;
		case "4":
			return FORCED_END;
		default:
			return UNKNOW;
		}
	}

}
