/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.ui.config.schedule;

import java.util.Collection;

import org.apache.iotdb.ui.config.ContinuousIoTDBSession;
import org.apache.iotdb.ui.config.ExporterConfig;
import org.apache.iotdb.ui.config.tsdatasource.SessionDataSetWrapper;
import org.apache.iotdb.ui.config.websocket.WebsocketConfiguration;
import org.apache.iotdb.ui.config.websocket.WebsocketEndPoint;
import org.apache.iotdb.ui.controller.UserController;
import org.apache.iotdb.ui.model.CaptchaWrapper;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
public class TimerConfig {

	@Autowired
	private ExporterConfig exporterConfig;

	@Autowired
	private ExporterTimerBucket exporterTimerBucket;

	@Autowired
	@Qualifier("exporterTaskExecutor")
	private ThreadPoolTaskExecutor exporterTaskExecutor;

	public static Long cou = System.currentTimeMillis() / 1000;

	public static Long cou1 = System.currentTimeMillis() / 1000;

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

	@Scheduled(cron = "0/1 * * * * *")
	public void pullExporter() {
		cou1++;
		exporterTimerBucket.getExporterTimerMap().forEach((k, v) -> {
			int period = v.getPeriod() >= 5 ? v.getPeriod() : 5;
			if (cou1 % period == 0) {
				exporterTaskExecutor.execute(new Runnable() {
					public void run() {
						try {
							exporterConfig.readMetrics(v.getEndPoint(), v.getCode());
						} catch (Exception e) {
						}
					}
				});
			}
		});
	}
}
