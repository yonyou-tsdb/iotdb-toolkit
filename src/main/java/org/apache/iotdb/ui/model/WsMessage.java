package org.apache.iotdb.ui.model;

public enum WsMessage {

	CONNECT_SUCCESS("CONNECT_SUCCESS", "Connect Success"), EXPORT_START("EXPORT_START", "Export Start"),
	EXPORT_ONGOING("EXPORT_ONGOING", "Export Ongoing"), EXPORT_INTERRUPT("EXPORT_INTERRUPT", "Export Interrupt"),
	EXPORT_FINISH("EXPORT_FINISH", "Export Finish"), IMPORT_START("IMPORT_START", "Import Start"),
	IMPORT_ONGOING("IMPORT_ONGOING", "Import Ongoing"), IMPORT_INTERRUPT("IMPORT_INTERRUPT", "Import Interrupt"),
	IMPORT_FINISH("IMPORT_FINISH", "Import Finish"), FETCH_JOBS("FETCH_JOBS", "Fetch Jobs"),
	ADD_JOB("ADD_JOB", "Add Job");

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
