package org.apache.iotdb.ui.config;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class DynamicTask implements SchedulingConfigurer {

	/**
	 * corePoolSize = 0，maximumPoolSize = Integer.MAX_VALUE，即线程数量几乎无限制；
	 * keepAliveTime = 60s，线程空闲60s后自动结束。 workQueue 为 SynchronousQueue
	 * 同步队列，这个队列类似于一个接力棒，入队出队必须同时传递，因为CachedThreadPool线程创建无限制，不会有队列等待，所以使用SynchronousQueue；
	 */
	private static final ExecutorService es = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS,
			new SynchronousQueue<>());

	private volatile ScheduledTaskRegistrar registrar;
	private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, CronTask> cronTasks = new ConcurrentHashMap<>();

	private ConcurrentHashMap<String, TaskConstant> taskConstantMap = new ConcurrentHashMap<>();

	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		this.registrar = registrar;
		this.registrar.addTriggerTask(() -> {
			if (!CollectionUtils.isEmpty(taskConstantMap)) {
				System.out.println("检测动态定时任务列表...数量：" + taskConstantMap.size());
				Map<String, TimingTask> ttm = new HashMap<>();
				taskConstantMap.entrySet().forEach(e -> {
					TimingTask tt = new TimingTask();
					tt.setExpression(e.getValue().getCron());
					tt.setTaskId("dynamic-task-" + e.getValue().getTaskId());
					tt.setRule(e.getValue().getRule());
					ttm.put(tt.getTaskId(), tt);
				});
				this.refreshTasks(ttm);
			} else {
				System.out.println("检测动态定时任务列表...数量：" + taskConstantMap.size());
				this.refreshTasks(Collections.emptyMap());
			}
		}, triggerContext -> new PeriodicTrigger(5L, TimeUnit.SECONDS).nextExecutionTime(triggerContext));
	}

	public ConcurrentHashMap<String, TaskConstant> getTaskConstantMap() {
		return taskConstantMap;
	}

	private void refreshTasks(Map<String, TimingTask> tasks) {
		// 取消已经删除的策略任务
		Set<String> taskIds = scheduledFutures.keySet();
		for (String taskId : taskIds) {
			if (!exists(tasks, taskId)) {
				scheduledFutures.get(taskId).cancel(false);
			}
		}
		for (TimingTask tt : tasks.values()) {
			String expression = tt.getExpression();
			if (StringUtils.isBlank(expression) || !CronSequenceGenerator.isValidExpression(expression)) {
				System.out.println("定时任务DynamicTask cron表达式不合法: " + expression);
				continue;
			}
			// 如果配置一致，则不需要重新创建定时任务
			if (scheduledFutures.containsKey(tt.getTaskId())
					&& cronTasks.get(tt.getTaskId()).getExpression().equals(expression)) {
				continue;
			}
			// 如果策略执行时间发生了变化，则取消当前策略的任务
			if (scheduledFutures.containsKey(tt.getTaskId())) {
				scheduledFutures.remove(tt.getTaskId()).cancel(false);
				taskConstantMap.remove(tt.getTaskId());
				cronTasks.remove(tt.getTaskId());
			}
			CronTask task = new CronTask(tt, expression);
			ScheduledFuture<?> future = Objects.requireNonNull(registrar.getScheduler()).schedule(task.getRunnable(),
					task.getTrigger());
			cronTasks.put(tt.getTaskId(), task);
			assert future != null;
			scheduledFutures.put(tt.getTaskId(), future);
		}
	}

	private boolean exists(Map<String, TimingTask> tasks, String taskId) {
		return tasks.containsKey(taskId);
	}

	@PreDestroy
	public void destroy() {
		this.registrar.destroy();
	}

	public static class TaskConstant {
		private String cron;
		private String taskId;
		private String rule;

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
	}

	private static class TimingTask implements Runnable {
		private String expression;

		private String taskId;

		private String rule;

		public String getRule() {
			return rule;
		}

		public void setRule(String rule) {
			this.rule = rule;
		}

		public String getTaskId() {
			return taskId;
		}

		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}

		@Override
		public void run() {

			es.submit(() -> {
				// 这里写业务方法
				System.out.println("执行定时任务:" + this.getTaskId() + ",执行时间： " + "，" + LocalDateTime.now().toLocalTime()
						+ "，" + this.getRule() + "，线程：" + Thread.currentThread().getName());

			});

		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE, false, false,
					TimingTask.class);
		}

	}

}
