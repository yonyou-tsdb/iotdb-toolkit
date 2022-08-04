package org.apache.iotdb.ui.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.iotdb.ui.condition.TaskCondition;
import org.apache.iotdb.ui.config.DistributedSnowflakeKeyGenerator2;
import org.apache.iotdb.ui.config.TaskWrapper;
import org.apache.iotdb.ui.config.schedule.TaskTimerBucket;
import org.apache.iotdb.ui.config.schedule.TimerConfig;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.TaskDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.iotdb.ui.model.TaskType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.assertj.core.util.Lists;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.yonyou.iotdb.utils.core.pipeline.context.model.CompressEnum;

import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.pagination.Order;
import indi.mybatis.flying.pagination.Page;
import indi.mybatis.flying.pagination.PageParam;
import indi.mybatis.flying.pagination.SortParam;
import io.swagger.annotations.Api;

@CrossOrigin
@RestController
@Api(value = "Schedule API")
public class ScheduleController {

	FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

	public static final List<TaskStatus> TASK_STATUS_LIST = Lists.list(new TaskStatus[] { TaskStatus.NOT_START,
			TaskStatus.IN_PROGRESS, TaskStatus.NORMAL_END, TaskStatus.ABEND, TaskStatus.FORCED_END });

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private TaskTimerBucket taskTimerBucket;

	@Autowired
	private TaskWrapper taskWrapper;

	@Autowired
	private TimerConfig timerConfig;

	@RequestMapping(value = "/api/schedule/task/all", method = { RequestMethod.POST })
	public BaseVO<Object> taskAll(HttpServletRequest request, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "taskType", required = false) TaskType taskType,
			@RequestParam(value = "taskStatus", required = false) List<TaskStatus> taskStatusList) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		TaskCondition tc = new TaskCondition();
		tc.setUserId(user.getId());
		tc.setType(taskType);
		if (taskStatusList == null || taskStatusList.isEmpty()) {
			taskStatusList = TASK_STATUS_LIST;
		}
		tc.setStatusIn(taskStatusList);
		Date now = LocalDateTime.now().toDate();
		tc.setLimiter(new PageParam(pageNum, pageSize));
		tc.setSorter(new SortParam(new Order("start_window_from", Conditionable.Sequence.ASC),
				new Order("priority", Conditionable.Sequence.ASC), new Order("id", Conditionable.Sequence.ASC)));
		List<Task> list = taskDao.selectAll(tc);
		Page<Task> page = new Page<>(list, tc.getLimiter());
		return BaseVO.success(page);
	}

	@RequestMapping(value = "/api/schedule/task/add", method = { RequestMethod.POST })
	public BaseVO<Object> taskAdd(HttpServletRequest request, @RequestParam("type") TaskType type,
			@RequestParam(Task.SETTING_CONNECTID) Long connectId,
			@RequestParam(value = Task.SETTING_DEVICE, required = false) String device,
			@RequestParam(value = Task.SETTING_MEASUREMENTLIST, required = false) String measurementList,
			@RequestParam(value = Task.SETTING_WHERECLAUSE, required = false) String whereClause,
			@RequestParam(value = Task.SETTING_FILEFOLDER, required = false) String fileFolder,
			@RequestParam(Task.SETTING_COMPRESS) CompressEnum compress,
			@RequestParam("timeWindowStart") Long timeWindowStart, @RequestParam("priority") Integer priority)
			throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Date timeWindowStartFrom = new Date(timeWindowStart);
//		Date timeWindowEndTo = new Date(timeWindowEnd);
		Task task = new Task();
		task.setName(new StringBuilder(type.name()).append('-').append(DistributedSnowflakeKeyGenerator2.getId())
				.append('-').append(compress).toString());
		task.setUserId(user.getId());
		task.setStartWindowFrom(timeWindowStartFrom);
//		task.setStartWindowTo(timeWindowEndTo);
		task.setType(type);
		task.setStatus(TaskStatus.NOT_START);
		task.setPriority(priority);
		Date now = LocalDateTime.now().toDate();
		task.setCreateTime(now);
		task.setUpdateTime(now);
		JSONObject setting = new JSONObject();
		setting.put(Task.SETTING_COMPRESS, compress);
		setting.put(Task.SETTING_CONNECTID, connectId);
		setting.put(Task.SETTING_DEVICE, device);
		setting.put(Task.SETTING_MEASUREMENTLIST, measurementList);
		setting.put(Task.SETTING_WHERECLAUSE, whereClause);
		setting.put(Task.SETTING_FILEFOLDER, fileFolder);
		task.setSetting(setting);
		int i = taskDao.insert(task);
		if (i == 1) {
			taskTimerBucket.getTaskTimerMap().put(task.key(), task);
		}
		String info = String.format("%s %s (%s)", type, fastDateFormat.format(timeWindowStartFrom), priority);
		return BaseVO.success(info, null);
	}

	@RequestMapping(value = "/api/schedule/task/update", method = { RequestMethod.POST })
	public BaseVO<Object> taskUpdate(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam(value = Task.SETTING_DEVICE, required = false) String device,
			@RequestParam(value = Task.SETTING_MEASUREMENTLIST, required = false) String measurementList,
			@RequestParam(value = Task.SETTING_WHERECLAUSE, required = false) String whereClause,
			@RequestParam(value = Task.SETTING_FILEFOLDER, required = false) String fileFolder,
			@RequestParam(Task.SETTING_COMPRESS) CompressEnum compress,
			@RequestParam("timeWindowStart") Long timeWindowStart, @RequestParam("timeWindowEnd") Long timeWindowEnd,
			@RequestParam("priority") Integer priority) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Task t = new Task();
		t.setId(id);
		t.setUserId(user.getId());
		Task task = taskDao.selectOne(t);
		if (task == null) {
			return new BaseVO<>(FeedbackError.TASK_GET_FAIL, null);
		}
		if (!TaskStatus.NOT_START.equals(task.getStatus())) {
			return new BaseVO<>(FeedbackError.TASK_EDIT_FAIL_FOR_ALREADY_START, null);
		}
		String oldKey = task.key();
		if (task.getSetting() == null) {
			task.setSetting(new JSONObject());
		}
		if (device != null) {
			task.getSetting().put(Task.SETTING_DEVICE, device);
		}
		if (whereClause != null) {
			task.getSetting().put(Task.SETTING_WHERECLAUSE, whereClause);
		}
		if (fileFolder != null) {
			task.getSetting().put(Task.SETTING_FILEFOLDER, fileFolder);
		}
		if (measurementList != null) {
			task.getSetting().put(Task.SETTING_MEASUREMENTLIST, measurementList);
		}
		task.getSetting().put(Task.SETTING_COMPRESS, compress);
		Date timeWindowStartFrom = new Date(timeWindowStart);
		Date timeWindowEndTo = new Date(timeWindowEnd);
		task.setStartWindowFrom(timeWindowStartFrom);
		task.setStartWindowTo(timeWindowEndTo);
		task.setPriority(priority);
		int i = taskDao.update(task);
		if (i == 1) {
			taskTimerBucket.getTaskTimerMap().remove(oldKey);
			taskTimerBucket.getTaskTimerMap().put(task.key(), task);
		}
		String info = String.format("%s %s (%s)", task.getType(), fastDateFormat.format(task.getStartWindowFrom()),
				task.getPriority());
		return BaseVO.success(info, task);
	}

	@RequestMapping(value = "/api/schedule/task/view", method = { RequestMethod.POST })
	public BaseVO<Object> taskView(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Task t = new Task();
		t.setId(id);
		t.setUserId(user.getId());
		Task task = taskDao.selectOne(t);
		if (task == null) {
			return new BaseVO<>(FeedbackError.TASK_GET_FAIL, null);
		} else {
			if (task.getSetting() != null) {
				Long connectId = task.getSetting().getLong(Task.SETTING_CONNECTID);
				if (connectId != null) {
					Connect connect = connectDao.select(connectId);
					if (connect != null) {
						task.getSetting().put("connectDesc",
								String.format("%s@%s:%s", connect.getUsername(), connect.getHost(), connect.getPort()));
					}
				}
			} else {
				task.setSetting(new JSONObject());
			}
			return BaseVO.success(task);
		}
	}

	@RequestMapping(value = "/api/schedule/task/delete", method = { RequestMethod.POST })
	public BaseVO<Object> taskDelete(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Task t = new Task();
		t.setId(id);
		t.setUserId(user.getId());
		Task task = taskDao.selectOne(t);
		if (task == null) {
			return new BaseVO<>(FeedbackError.TASK_GET_FAIL, null);
		}
		if (!TaskStatus.NOT_START.equals(task.getStatus())) {
			return new BaseVO<>(FeedbackError.TASK_DELETE_FAIL_FOR_ALREADY_START, null);
		}
		int c = taskDao.delete(task);
		if (c != 1) {
			return new BaseVO<>(FeedbackError.TASK_DELETE_FAIL, null);
		} else {
			taskTimerBucket.getTaskTimerMap().remove(task.key());
		}
		String info = String.format("%s %s (%s)", task.getType(), fastDateFormat.format(task.getStartWindowFrom()),
				task.getPriority());
		return BaseVO.success(info, null);
	}

	@RequestMapping(value = "/api/schedule/task/process", method = { RequestMethod.POST })
	public BaseVO<Object> taskProcess(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Long process = taskWrapper.getProcess(id);
		if (process == null) {
			return new BaseVO<>(FeedbackError.TASK_CHECK_PROCESS_FAIL, null);
		}
		boolean b = taskWrapper.isFinish();
		return BaseVO.success(process.toString(), b);
	}

	@RequestMapping(value = "/api/schedule/task/shutdown", method = { RequestMethod.POST })
	public BaseVO<Object> taskShutdown(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		boolean b = taskWrapper.shutdown(id);
		if (b) {
			timerConfig.endTask(TaskStatus.FORCED_END);
			return BaseVO.success(null);
		} else {
			timerConfig.forcedEndTask(TaskStatus.FORCED_END, id);
			return new BaseVO<>(FeedbackError.TASK_SHUTDOWN_FAIL, null);
		}
	}
}
