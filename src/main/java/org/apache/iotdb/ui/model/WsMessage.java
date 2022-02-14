package org.apache.iotdb.ui.model;

public enum WsMessage {

	CONNECT_SUCCESS("CONNECT_SUCCESS", "连接成功"), EXPORT_START("EXPORT_START", "导出开始"),
	EXPORT_ONGOING("EXPORT_ONGOING", "导出进行中"), EXPORT_INTERRUPT("EXPORT_INTERRUPT", "导出中止"),
	EXPORT_FINISH("EXPORT_FINISH", "导出结束"), IMPORT_START("IMPORT_START", "导入开始"),
	IMPORT_ONGOING("IMPORT_ONGOING", "导入进行中"), IMPORT_INTERRUPT("IMPORT_INTERRUPT", "导入中止"),
	IMPORT_FINISH("IMPORT_FINISH", "导入结束"), FETCH_JOBS("FETCH_JOBS", "获取任务列表"), ADD_JOB("ADD_JOB", "增加任务");

	private final String value;

	private final String description;

	private WsMessage(String value, String description) {
		this.value = value;
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public static WsMessage forValue(String value) {
		if (value == null) {
			return CONNECT_SUCCESS;
		}
		switch (value) {
		case "CONNECT_SUCCESS":
			return CONNECT_SUCCESS;
		case "EXPORT_START":
			return EXPORT_START;
		case "EXPORT_ONGOING":
			return EXPORT_ONGOING;
		case "EXPORT_INTERRUPT":
			return EXPORT_INTERRUPT;
		case "EXPORT_FINISH":
			return EXPORT_FINISH;
		case "IMPORT_START":
			return IMPORT_START;
		case "IMPORT_ONGOING":
			return IMPORT_ONGOING;
		case "IMPORT_INTERRUPT":
			return IMPORT_INTERRUPT;
		case "IMPORT_FINISH":
			return IMPORT_FINISH;
		case "FETCH_JOBS":
			return FETCH_JOBS;
		case "ADD_JOB":
			return ADD_JOB;
		default:
			return CONNECT_SUCCESS;
		}
	}

}
