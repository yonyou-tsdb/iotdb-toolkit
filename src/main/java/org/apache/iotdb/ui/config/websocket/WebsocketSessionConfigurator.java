package org.apache.iotdb.ui.config.websocket;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;

@Component
public class WebsocketSessionConfigurator extends Configurator {
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		Subject subject = SecurityUtils.getSubject();
		Session shiroSession = subject.getSession();
		sec.getUserProperties().put(WebsocketEndPoint.SHIRO_SESSION, shiroSession);
	}
}
