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

import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.ui.config.ContinuousIoTDBSession;
import org.apache.iotdb.ui.config.ExporterConfig;
import org.apache.iotdb.ui.config.MonitorServerConfig;
import org.apache.iotdb.ui.config.TaskWrapper;
import org.apache.iotdb.ui.config.tsdatasource.SessionDataSetWrapper;
import org.apache.iotdb.ui.config.websocket.WebsocketConfiguration;
import org.apache.iotdb.ui.config.websocket.WebsocketEndPoint;
import org.apache.iotdb.ui.controller.UserController;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.TaskDao;
import org.apache.iotdb.ui.model.CaptchaWrapper;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.iotdb.ui.model.TaskType;
import org.apache.shiro.session.Session;
import org.assertj.core.util.Lists;
import org.joda.time.LocalDateTime;
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
import com.yonyou.iotdb.utils.core.pipeline.context.model.ImportModel;

import reactor.core.Disposable;

@Configuration
@EnableScheduling
public class TimerConfig {

	@Autowired
	private ConnectDao connectDao;

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

	@Scheduled(cron = "0 0/1 * * * *")
	public void heartBeat() {
		Long cou = System.currentTimeMillis() / 1000;
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

	@Scheduled(cron = "0/1 * * * * *")
	public void pullExporter() {
		Long cou1 = System.currentTimeMillis() / 1000;
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

	@Scheduled(cron = "0/1 * * * * *")
	public void dealTask() {
		Iterator<Entry<String, Task>> it = taskTimerBucket.getTaskTimerMap().entrySet().iterator();
		Long cou1 = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Task> e = it.next();
			Task task = e.getValue();
			TaskStatus t = task.getStatus();
			if (TaskStatus.NORMAL_END.equals(t) || TaskStatus.ABEND.equals(t) || TaskStatus.FORCED_END.equals(t)) {
				it.remove();
			} else if (TaskStatus.NOT_START.equals(t) && task.getStartWindowFrom().getTime() <= (cou1 + 1000)) {
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.setStartTime(LocalDateTime.now().toDate());
				taskDao.update(task);
				handleTask(task);
				break;
			} else {
				break;
			}
		}
	}

	public void endTask(TaskStatus status) {
		Task task = taskWrapper.getTask();
		task.setStatus(status);
		task.setResultRows(taskWrapper.getProcess(task.getId()));
		task.setEndTime(LocalDateTime.now().toDate());
		Long cost = (task.getEndTime().getTime() - task.getStartTime().getTime()) / 1000;
		task.setTimeCost(cost.intValue());
		int i = taskDao.update(task);
		if (i == 1) {
			taskWrapper.setTask(null);
			taskTimerBucket.getTaskTimerMap().put(task.key(), task);
		}
	}

	public void forcedEndTask(TaskStatus status, Long taskId) {
		Task task = taskDao.select(taskId);
		if (TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
			task.setStatus(status);
			task.setResultRows(0L);
			Long cost = (task.getEndTime().getTime() - task.getStartTime().getTime()) / 1000;
			task.setTimeCost(cost.intValue());
			int i = taskDao.update(task);
			if (i == 1) {
				taskTimerBucket.getTaskTimerMap().put(task.key(), task);
			}
		}
	}

	private Disposable handleTask(Task task) {
		System.out.println(Thread.currentThread().getName());
		taskWrapper.setTask(task);
		if (TaskType.EXPORT.equals(task.getType())) {
			ExportModel exportModel = new ExportModel();
			exportModel.setCharSet("utf8");
			exportModel.setFileSinkStrategyEnum(FileSinkStrategyEnum.EXTRA_CATALOG);
			exportModel.setNeedTimeseriesStructure(true);
			exportModel.setParallelism(2);
			if (task.getSetting() != null) {
				exportModel.setIotdbPath(task.getSetting().getString(Task.SETTING_DEVICE));
				if (task.getSetting().getString(Task.SETTING_COMPRESS) != null) {
					exportModel
							.setCompressEnum(CompressEnum.valueOf(task.getSetting().getString(Task.SETTING_COMPRESS)));
				}
				if (task.getSetting().getString(Task.SETTING_WHERECLAUSE) != null) {
					exportModel.setWhereClause(task.getSetting().getString(Task.SETTING_WHERECLAUSE));
				}
				if (task.getSetting().getString(Task.SETTING_MEASUREMENTLIST) != null) {
					String[] measurements = task.getSetting().getString(Task.SETTING_MEASUREMENTLIST).split(",");
					exportModel.setMeasurementList(Lists.list(measurements));
				}
				if (task.getSetting().getLong(Task.SETTING_CONNECTID) != null) {
					Connect connect = connectDao.selectUnsafe(task.getSetting().getLong(Task.SETTING_CONNECTID));
					if (connect != null) {
						org.apache.iotdb.session.Session session = new org.apache.iotdb.session.Session(
								connect.getHost(), connect.getPort(), connect.getUsername(), connect.getPassword());
						try {
							session.open();
						} catch (IoTDBConnectionException e) {
						}
						exportModel.setSession(session);
					}
				}
			}
			exportModel
					.setFileFolder(new StringBuilder(monitorServerConfig.getTemp()).append(task.getName()).toString());
			exportModel.setConsumer(s -> {
				switch (s) {
				case ON_ERROR:
					if (taskWrapper.getTask() != null) {
						endTask(TaskStatus.ABEND);
					}
					break;
				case ON_COMPLETE:
					if (taskWrapper.isFinish() && taskWrapper.getTask() != null) {
						endTask(TaskStatus.NORMAL_END);
					}
					break;
				default:
				}
			});
			return taskWrapper.start(exportModel);
		} else {
			ImportModel importModel = new ImportModel();
			importModel.setCharSet("utf8");
			importModel.setFileSinkStrategyEnum(FileSinkStrategyEnum.EXTRA_CATALOG);
			importModel.setNeedTimeseriesStructure(true);
			importModel.setParallelism(2);
			if (task.getSetting() != null) {
				importModel.setFileFolder(task.getSetting().getString(Task.SETTING_FILEFOLDER));
				if (task.getSetting().getString(Task.SETTING_COMPRESS) != null) {
					importModel
							.setCompressEnum(CompressEnum.valueOf(task.getSetting().getString(Task.SETTING_COMPRESS)));
				}
				if (task.getSetting().getLong(Task.SETTING_CONNECTID) != null) {
					Connect connect = connectDao.selectUnsafe(task.getSetting().getLong(Task.SETTING_CONNECTID));
					if (connect != null) {
						org.apache.iotdb.session.Session session = new org.apache.iotdb.session.Session(
								connect.getHost(), connect.getPort(), connect.getUsername(), connect.getPassword());
						try {
							session.open();
						} catch (IoTDBConnectionException e) {
						}
						importModel.setSession(session);
					}
				}
			}
			importModel.setConsumer(s -> {
				switch (s) {
				case ON_ERROR:
					if (taskWrapper.getTask() != null) {
						endTask(TaskStatus.ABEND);
					}
					break;
				case ON_COMPLETE:
					if (taskWrapper.isFinish() && taskWrapper.getTask() != null) {
						endTask(TaskStatus.NORMAL_END);
					}
					break;
				default:
				}
			});
			return taskWrapper.start(importModel);
		}
	}
}
