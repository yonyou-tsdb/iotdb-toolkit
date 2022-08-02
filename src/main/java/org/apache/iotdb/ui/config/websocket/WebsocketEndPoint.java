package org.apache.iotdb.ui.config.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.iotdb.ui.config.DistributedSnowflakeKeyGenerator2;
import org.apache.iotdb.ui.model.JobType;
import org.apache.iotdb.ui.model.UserJob;
import org.apache.iotdb.ui.model.WsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

@ConditionalOnMissingClass("org.apache.iotdb.ui.config.InitialConfig")
@ServerEndpoint(value = "/api/websocket/{sid}", configurator = WebsocketSessionConfigurator.class)
@Component
public class WebsocketEndPoint {

	static final Logger log = LoggerFactory.getLogger(WebsocketEndPoint.class);

	public static final String SHIRO_SESSION = "shiroSession";

	public static final String JOBS = "jobs";

	private Session wssession;

	private String wssessionId;

	private boolean exportInterrupt;

	private Map<Long, UserJob> jobMap;

	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String id) {
		wssession = session;
		wssessionId = session.getId();
		WebsocketConfiguration.webSocketIdMap.put(id, this);
		sendMessage(new WsMessageModel(WsMessage.CONNECT_SUCCESS));
	}

	@OnClose
	public void onClose(Session session) {
		try {
			WebsocketConfiguration.webSocketIdMap.remove(session.getId());// sid从idSet中删除
		} catch (Exception e) {
		}
	}

	@OnMessage
	public void onMessage(String message, @PathParam("sid") String id, Session session) {
		JSONObject json = null;
		try {
			json = JSONObject.parseObject(message);
		} catch (Exception e) {
			json = new JSONObject();
			json.put("type", message);
		}
		WsMessage wsMessage = WsMessage.forValue(json.getString("type"));

		switch (wsMessage) {
		case EXPORT_INTERRUPT:
		case IMPORT_INTERRUPT:
			if (json.getLong("key") != null) {
				getJobMap().remove(json.getLong("key"));
			}
			break;
		default:
		}

	}

	@OnError
	public void onError(Session session, Throwable error) {
		log.error(new StringBuilder("websocket ").append(wssessionId).append(" error").toString(), error);
	}

	private void sendMessage(String message) {
		try {
			wssession.getBasicRemote().sendText(message);
		} catch (IOException e) {
			log.error("websocket" + wssessionId + " IO异常");
		}
	}

	public void sendMessage(WsMessageModel message) {
		sendMessage(JSONObject.toJSONString(message));
	}

	public Map<Long, UserJob> getJobMap() {
		if (jobMap == null) {
			jobMap = new ConcurrentSkipListMap<>();
		}
		return jobMap;
	}

	public Long addJob(JobType type) {
		try {
			Long id = DistributedSnowflakeKeyGenerator2.getId();
			UserJob job = new UserJob(id, type);
			getJobMap().put(id, job);
			return id;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isOpen() {
		return wssession.isOpen();
	}

	public Session getWssession() {
		return wssession;
	}

	public void setWssession(Session wssession) {
		this.wssession = wssession;
	}

	public boolean isExportInterrupt() {
		return exportInterrupt;
	}

	public void setExportInterrupt(boolean exportInterrupt) {
		this.exportInterrupt = exportInterrupt;
	}

	public String getWssessionId() {
		return wssessionId;
	}

	public void setWssessionId(String wssessionId) {
		this.wssessionId = wssessionId;
	}
}
