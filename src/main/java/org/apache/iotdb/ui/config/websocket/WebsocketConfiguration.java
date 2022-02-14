package org.apache.iotdb.ui.config.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebsocketConfiguration {

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	// 用来存放每个客户端对应的WebsocketEndPoint对象
	protected static Map<String, WebsocketEndPoint> webSocketIdMap = new ConcurrentHashMap<>();

	// 静态变量，用来记录当前在线连接数
	private static AtomicInteger onlineCount = new AtomicInteger(0);

	public static Map<String, WebsocketEndPoint> getWebSocketIdMap() {
		return webSocketIdMap;
	}

	public static synchronized Integer getOnlineCount() {
		return Integer.valueOf(onlineCount.toString());
	}

	public static synchronized void addOnlineCount() {
		onlineCount.set(getOnlineCount() + 1);
	}

	public static synchronized void subOnlineCount() {
		onlineCount.set(getOnlineCount() - 1);
	}
}
