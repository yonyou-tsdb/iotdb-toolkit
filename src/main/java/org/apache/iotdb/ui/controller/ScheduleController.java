package org.apache.iotdb.ui.controller;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.iotdb.ui.condition.TaskCondition;
import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.mapper.TaskDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.iotdb.ui.model.TaskType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	@Autowired
	private TaskDao taskDao;

	@RequestMapping(value = "/api/schedule/task/all", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskAll(HttpServletRequest request, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "taskType", required = false) TaskType taskType,
			@RequestParam(value = "taskStatus", required = false) TaskStatus taskStatus) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		TaskCondition tc = new TaskCondition();
		tc.setUserId(user.getId());
		tc.setLimiter(new PageParam(pageNum, pageSize));
		tc.setSorter(new SortParam(new Order("start_window_from", Conditionable.Sequence.ASC),
				new Order("id", Conditionable.Sequence.ASC)));
		List<Task> list = taskDao.selectAll(tc);
		Page<Task> page = new Page<>(list, tc.getLimiter());
		return BaseVO.success(page);
	}

	@RequestMapping(value = "/api/schedule/task/add", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskAdd(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		return null;
	}

	@RequestMapping(value = "/api/schedule/task/update", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskUpdate(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		return null;
	}

	@RequestMapping(value = "/api/schedule/task/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> taskDelete(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		return null;
	}
}
