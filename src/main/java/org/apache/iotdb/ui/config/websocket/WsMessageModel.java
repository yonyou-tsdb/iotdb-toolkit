package org.apache.iotdb.ui.config.websocket;

import org.apache.iotdb.ui.model.WsMessage;

public class WsMessageModel {

	public WsMessageModel(WsMessage type, String message, Long key) {
		this.type = type;
		this.message = message;
		this.key = key;
	}

	public WsMessageModel(WsMessage type, Long key) {
		this.type = type;
		this.message = type.getDescription();
		this.key = key;
	}

	public WsMessageModel(WsMessage type) {
		this.type = type;
		this.message = type.getDescription();
	}

	private WsMessage type;

	private String message;

	private Long key;

	public WsMessage getType() {
		return type;
	}

	public void setType(WsMessage type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

}
