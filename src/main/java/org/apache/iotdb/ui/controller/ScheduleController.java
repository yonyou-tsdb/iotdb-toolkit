package org.apache.iotdb.ui.controller;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.iotdb.ui.condition.TaskCondition;
import org.apache.iotdb.ui.config.TaskWrapper;
import org.apache.iotdb.ui.config.schedule.TaskTimerBucket;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.TaskDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.iotdb.ui.model.TaskType;
import org.apache.iotdb.ui.util.MessageUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.yonyou.iotdb.utils.core.ExportStarter;
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

	public static final TaskStatus[] c = { TaskStatus.NOT_START, TaskStatus.IN_PROGRESS, TaskStatus.NORMAL_END,
			TaskStatus.FORCED_END };

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private TaskTimerBucket taskTimerBucket;

	@Autowired
	private TaskWrapper taskWrapper;

	@Autowired
	private ExportStarter exportStarter;

	@RequestMapping(value = "/api/schedule/task/all", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskAll(HttpServletRequest request, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "timeline", required = false) String timeline,
			@RequestParam(value = "taskType", required = false) TaskType taskType,
			@RequestParam(value = "taskStatus", required = false) List<TaskStatus> taskStatusList) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		TaskCondition tc = new TaskCondition();
		tc.setUserId(user.getId());
		tc.setType(taskType);
		if (taskStatusList == null || taskStatusList.isEmpty()) {
			taskStatusList = Lists.list(c);
		}
		tc.setStatusIn(taskStatusList);
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		if ("history".equals(timeline)) {
			tc.setStartWindowToLessThan(now);
		} else {
			tc.setStartWindowToGreaterOrEqual(now);
		}
		tc.setLimiter(new PageParam(pageNum, pageSize));
		tc.setSorter(new SortParam(new Order("start_window_from", Conditionable.Sequence.ASC),
				new Order("priority", Conditionable.Sequence.ASC), new Order("id", Conditionable.Sequence.ASC)));
		List<Task> list = taskDao.selectAll(tc);
		Page<Task> page = new Page<>(list, tc.getLimiter());
		return BaseVO.success(page);
	}

	@RequestMapping(value = "/api/schedule/task/add", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskAdd(HttpServletRequest request, @RequestParam("type") TaskType type,
			@RequestParam("connectId") Long connectId, @RequestParam(value = "device", required = false) String device,
			@RequestParam(value = "whereClause", required = false) String whereClause,
			@RequestParam("compress") CompressEnum compress, @RequestParam("timeWindowStart") Long timeWindowStart,
			@RequestParam("timeWindowEnd") Long timeWindowEnd, @RequestParam("priority") Integer priority)
			throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Date timeWindowStartFrom = new Date(timeWindowStart);
		Date timeWindowEndTo = new Date(timeWindowEnd);
		Task task = new Task();
		task.setUserId(user.getId());
		task.setStartWindowFrom(timeWindowStartFrom);
		task.setStartWindowTo(timeWindowEndTo);
		task.setType(type);
		task.setStatus(TaskStatus.NOT_START);
		task.setPriority(priority);
		Date now = Calendar.getInstance().getTime();
		task.setCreateTime(now);
		task.setUpdateTime(now);
		JSONObject setting = new JSONObject();
		setting.put("compress", compress);
		setting.put("connectId", connectId);
		setting.put("device", device);
		setting.put("whereClause", whereClause);
		task.setSetting(setting);
		int i = taskDao.insert(task);
		if (i == 1) {
			taskTimerBucket.getTaskTimerMap().put(task.key(), task);
		}
		String info = String.format("%s %s -- %s (%s)", type, fastDateFormat.format(timeWindowStartFrom),
				fastDateFormat.format(timeWindowEndTo), priority);
		return BaseVO.success(info, null);
	}

	@RequestMapping(value = "/api/schedule/task/update", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskUpdate(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam(value = "device", required = false) String device,
			@RequestParam(value = "whereClause", required = false) String whereClause,
			@RequestParam("compress") CompressEnum compress, @RequestParam("timeWindowStart") Long timeWindowStart,
			@RequestParam("timeWindowEnd") Long timeWindowEnd, @RequestParam("priority") Integer priority)
			throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Task t = new Task();
		t.setId(id);
		t.setUserId(user.getId());
		Task task = taskDao.selectOne(t);
		if (task == null) {
			return new BaseVO<>(FeedbackError.TASK_GET_FAIL, MessageUtil.get(FeedbackError.TASK_GET_FAIL), null);
		}
		String oldKey = task.key();
		if (task.getSetting() == null) {
			task.setSetting(new JSONObject());
		}
		if (device != null) {
			task.getSetting().put("device", device);
		}
		if (whereClause != null) {
			task.getSetting().put("whereClause", whereClause);
		}
		task.getSetting().put("compress", compress);
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
		String info = String.format("%s %s -- %s (%s)", task.getType(),
				fastDateFormat.format(task.getStartWindowFrom()), fastDateFormat.format(task.getStartWindowTo()),
				task.getPriority());
		return BaseVO.success(info, task);
	}

	@RequestMapping(value = "/api/schedule/task/view", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskView(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Task t = new Task();
		t.setId(id);
		t.setUserId(user.getId());
		Task task = taskDao.selectOne(t);
		if (task == null) {
			return new BaseVO<>(FeedbackError.TASK_GET_FAIL, MessageUtil.get(FeedbackError.TASK_GET_FAIL), null);
		} else {
			if (task.getSetting() != null) {
				Long connectId = task.getSetting().getLong("connectId");
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

	@RequestMapping(value = "/api/schedule/task/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskDelete(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Task t = new Task();
		t.setId(id);
		t.setUserId(user.getId());
		Task task = taskDao.selectOne(t);
		if (task == null) {
			return new BaseVO<>(FeedbackError.TASK_GET_FAIL, MessageUtil.get(FeedbackError.TASK_GET_FAIL), null);
		}
		if (!TaskStatus.NOT_START.equals(task.getStatus())) {
			return new BaseVO<>(FeedbackError.TASK_DELETE_FAIL_FOR_ALREADY_START,
					MessageUtil.get(FeedbackError.TASK_DELETE_FAIL_FOR_ALREADY_START), null);
		}
		int c = taskDao.delete(task);
		if (c != 1) {
			return new BaseVO<>(FeedbackError.TASK_DELETE_FAIL, MessageUtil.get(FeedbackError.TASK_DELETE_FAIL), null);
		} else {
			taskTimerBucket.getTaskTimerMap().remove(task.key());
		}
		String info = String.format("%s %s -- %s (%s)", task.getType(),
				fastDateFormat.format(task.getStartWindowFrom()), fastDateFormat.format(task.getStartWindowTo()),
				task.getPriority());
		return BaseVO.success(info, null);
	}

	@RequestMapping(value = "/api/schedule/task/process", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskProcess(HttpServletRequest request) throws SQLException {
		Long process = taskWrapper.getProcess();
		boolean b = taskWrapper.isFinish();
		return BaseVO.success(process.toString(), b);
	}
}
