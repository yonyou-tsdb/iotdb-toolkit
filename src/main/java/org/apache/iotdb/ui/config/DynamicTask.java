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
package org.apache.iotdb.ui.config;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

@Component
public class DynamicTask implements SchedulingConfigurer {

	private final static Logger LOGGER = LoggerFactory.getLogger(DynamicTask.class);

	/**
	 * corePoolSize = 0，maximumPoolSize = Integer.MAX_VALUE，即线程数量几乎无限制；
	 * keepAliveTime = 60s，线程空闲60s后自动结束。 workQueue 为 SynchronousQueue
	 * 同步队列，这个队列类似于一个接力棒，入队出队必须同时传递，因为CachedThreadPool线程创建无限制，不会有队列等待，所以使用SynchronousQueue；
	 */
	private static final ExecutorService es = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
			new SynchronousQueue<>());

	private volatile ScheduledTaskRegistrar registrar;
	private ConcurrentHashMap<String, TaskConstant> taskConstantMap = new ConcurrentHashMap<>();

	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		DynamicTask.TaskConstant taskConstant = new DynamicTask.TaskConstant();
		taskConstant.setCron("a");
		taskConstant.setTaskId("test1");
		taskConstant.setRule("每隔5秒执行");
		taskConstantMap.put("test1", taskConstant);
		this.registrar = registrar;
		this.registrar.addTriggerTask(() -> {
			this.initTasks(taskConstantMap);
		}, triggerContext -> new PeriodicTrigger(Long.MAX_VALUE, TimeUnit.SECONDS).nextExecutionTime(triggerContext));
	}

	public ConcurrentHashMap<String, TaskConstant> getTaskConstantMap() {
		return taskConstantMap;
	}

	public void addTask(TaskConstant taskConstant) {
		String expression = taskConstant.getCron();
		if (StringUtils.isBlank(expression) || !CronSequenceGenerator.isValidExpression(expression)) {
			LOGGER.error(new StringBuilder("Scheduled task expression is invalid: ").append(expression).toString());
			return;
		}
		CronTask task = new CronTask(taskConstant, expression);
		ScheduledFuture<?> future = Objects.requireNonNull(registrar.getScheduler()).schedule(task.getRunnable(),
				task.getTrigger());
		taskConstant.setFuture(future);
		taskConstantMap.put(taskConstant.getTaskId(), taskConstant);
	}

	public void deleteTask(String taskId) {
		if (taskConstantMap.containsKey(taskId)) {
			TaskConstant task = taskConstantMap.get(taskId);
			if (task.getFuture() != null) {
				task.getFuture().cancel(false);
			}
			taskConstantMap.remove(taskId);
		}
	}

	public void updateTask(String taskId, TaskConstant taskConstant) {
		deleteTask(taskId);
		addTask(taskConstant);
	}

	private void initTasks(Map<String, TaskConstant> tasks) {
		for (TaskConstant tt : tasks.values()) {
			addTask(tt);
		}
	}

	@PreDestroy
	public void destroy() {
		this.registrar.destroy();
	}

	public static class TaskConstant implements Runnable {
		private String cron;
		private String taskId;
		private String rule;
		private ScheduledFuture<?> future;

		public String getRule() {
			return rule;
		}

		public void setRule(String rule) {
			this.rule = rule;
		}

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		public String getTaskId() {
			return taskId;
		}

		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}

		public ScheduledFuture<?> getFuture() {
			return future;
		}

		public void setFuture(ScheduledFuture<?> future) {
			this.future = future;
		}

		@Override
		public void run() {
			es.submit(() -> {
				// 这里写业务方法
				System.out.println("执行定时任务:" + this.getTaskId() + ",执行时间： " + "，" + LocalDateTime.now().toLocalTime()
						+ "，" + this.getRule() + "，线程：" + Thread.currentThread().getName());
			});
		}
	}
}
