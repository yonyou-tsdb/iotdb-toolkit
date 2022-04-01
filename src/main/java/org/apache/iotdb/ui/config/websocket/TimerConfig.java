package org.apache.iotdb.ui.config.websocket;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.iotdb.ui.config.ContinuousIoTDBSession;
import org.apache.iotdb.ui.config.tsdatasource.SessionDataSetWrapper;
import org.apache.iotdb.ui.controller.UserController;
import org.apache.iotdb.ui.model.CaptchaWrapper;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class TimerConfig {

	public static Long cou = System.currentTimeMillis() / 1000;

	protected static final Logger LOGGER = LoggerFactory.getLogger(TimerConfig.class);

	private Collection<SessionDataSetWrapper> sessionDataSetWrapperC = ContinuousIoTDBSession.continuousDataSetWrapperMap
			.values();

	private Collection<WebsocketEndPoint> copyOnWriteArraySet = WebsocketConfiguration.webSocketIdMap.values();

	private Collection<CaptchaWrapper> captchaWrapperSet = UserController.captchaMap.values();

	public static boolean sessionDataSetWrapperIsNotValid(Long cou, SessionDataSetWrapper sdsw) {
		if (cou - sdsw.getTimestamp() > 300) {
			return true;
		}
		return false;
	}

	public static boolean captchaIsNotValid(Long cou, CaptchaWrapper cw) {
		if (cou - cw.getTimestamp() > 300) {
			return true;
		}
		return false;
	}

	@Scheduled(cron = "0/1 * * * * *")
	public void sendmsg() {
		cou++;
		if (cou % 60 == 0) {
			copyOnWriteArraySet.forEach(c -> {
				try {
					Session subjectSession = (Session) c.getWssession().getUserProperties()
							.get(WebsocketEndPoint.SHIRO_SESSION);
					subjectSession.touch();
				} catch (Exception e) {
					WebsocketConfiguration.webSocketIdMap.remove(c.getWssessionId());
				}
			});
			sessionDataSetWrapperC.removeIf(e -> sessionDataSetWrapperIsNotValid(cou, e));
			captchaWrapperSet.removeIf(e -> captchaIsNotValid(cou, e));
		}
	}
}
