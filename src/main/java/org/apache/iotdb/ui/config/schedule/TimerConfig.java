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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.ui.config.ContinuousIoTDBSession;
import org.apache.iotdb.ui.config.ExporterConfig;
import org.apache.iotdb.ui.config.MonitorServerConfig;
import org.apache.iotdb.ui.config.TaskWrapper;
import org.apache.iotdb.ui.config.tsdatasource.SessionDataSetWrapper;
import org.apache.iotdb.ui.config.websocket.WebsocketConfiguration;
import org.apache.iotdb.ui.config.websocket.WebsocketEndPoint;
import org.apache.iotdb.ui.controller.UserController;
import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.mapper.TaskDao;
import org.apache.iotdb.ui.model.CaptchaWrapper;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.yonyou.iotdb.utils.core.pipeline.context.model.CompressEnum;
import com.yonyou.iotdb.utils.core.pipeline.context.model.ExportModel;
import com.yonyou.iotdb.utils.core.pipeline.context.model.FileSinkStrategyEnum;

@Configuration
@EnableScheduling
public class TimerConfig {

	@Autowired
	private ExporterConfig exporterConfig;

	@Autowired
	private ExporterTimerBucket exporterTimerBucket;

	@Autowired
	private MonitorServerConfig monitorServerConfig;

	@Autowired
	private TaskTimerBucket taskTimerBucket;

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private TaskWrapper taskWrapper;

	@Autowired
	@Qualifier("exporterTaskExecutor")
	private ThreadPoolTaskExecutor exporterTaskExecutor;

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
	public void couPlus() {
		cou++;
	}

	@Scheduled(cron = "0/1 * * * * *")
	public void sendmsg() {
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
		exporterTimerBucket.getExporterTimerMap().forEach((k, v) -> {
			int period = v.getPeriod() >= 5 ? v.getPeriod() : 5;
			if (cou % period == 0) {
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

	@Scheduled(cron = "0/1 * * * * *")
	public void dealTask() {
		if (taskWrapper.isFinish() && taskWrapper.getTask() != null) {
			Task task = taskWrapper.getTask();
			task.setStatus(TaskStatus.NORMAL_END);
			task.setResultRows(taskWrapper.getProcess());
			int i = taskDao.update(task);
			if (i == 1) {
				taskWrapper.setTask(null);
				taskTimerBucket.getTaskTimerMap().put(task.key(), task);
			}
		}
		if (cou % 60 == 0) {
			Iterator<Entry<String, Task>> it = taskTimerBucket.getTaskTimerMap().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Task> e = it.next();
				Task task = e.getValue();
				TaskStatus t = task.getStatus();
				if (task.getStartWindowTo().getTime() < cou * 1000 || TaskStatus.NORMAL_END.equals(t)
						|| TaskStatus.ABEND.equals(t) || TaskStatus.FORCED_END.equals(t)) {
					it.remove();
				} else if (TaskStatus.NOT_START.equals(t) && task.getStartWindowFrom().getTime() <= (cou + 1) * 1000) {
					task.setStatus(TaskStatus.IN_PROGRESS);
					taskDao.update(task);
					handleTask(task);
					break;
				} else {
					break;
				}
			}
		}
	}

	private void handleTask(Task task) {
		taskWrapper.setTask(task);
		ExportModel exportModel = new ExportModel();
		exportModel.setCharSet("utf8");
		exportModel.setCompressEnum(CompressEnum.GZIP);
		exportModel
				.setFileFolder(new StringBuilder(monitorServerConfig.getTemp()).append(UUID.randomUUID()).toString());
		exportModel.setFileSinkStrategyEnum(FileSinkStrategyEnum.EXTRA_CATALOG);
		if (CompressEnum.CSV.name().equals(task.getSetting().getString("compress"))) {
			exportModel.setIotdbPath("root._monitor.\"115.28.134.232\".**");
		} else {
			exportModel.setIotdbPath("root._monitor.\"172.20.48.111\".**");
		}
		exportModel.setNeedTimeseriesStructure(true);
		org.apache.iotdb.session.Session session = new org.apache.iotdb.session.Session("172.20.45.128", "6667", "root",
				"root");
		try {
			session.open();
		} catch (IoTDBConnectionException e) {
			e.printStackTrace();
		}
		exportModel.setSession(session);
		taskWrapper.start(exportModel);
	}
}
