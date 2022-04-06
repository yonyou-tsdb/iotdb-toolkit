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
package org.apache.iotdb.ui.controller;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;

import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.ui.condition.ConnectCondition;
import org.apache.iotdb.ui.config.DynamicSessionPoolConfig;
import org.apache.iotdb.ui.config.tsdatasource.DynamicSessionPool;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.BaseException;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.UserDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.TransactionService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.pagination.Order;
import indi.mybatis.flying.pagination.Page;
import indi.mybatis.flying.pagination.PageParam;
import indi.mybatis.flying.pagination.SortParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@Api(value = "Connection API")
public class ConnectionController {

	@Autowired
	@Qualifier("dynamicSessionPool")
	private DynamicSessionPool dynamicSessionPool;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private UserDao userDao;

	@ApiOperation(value = "/api/connection/test", notes = "/api/connection/test")
	@RequestMapping(value = "/api/connection/test", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionTest(@RequestParam("connectionUsername") String username,
			@RequestParam("connectionPassword") String password, @RequestParam("ip") String ip,
			@RequestParam("port") Integer port) {

		Connect connect = new Connect();
		connect.setUsername(username);
		connect.setPassword(password);
		connect.setHost(ip);
		connect.setPort(port);

		BaseVO<Object> ret = testConnection(connect);
		return ret;
	}

	@ApiOperation(value = "/api/connection/testById", notes = "/api/connection/testById")
	@RequestMapping(value = "/api/connection/testById", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionTestById(@RequestParam("connectionId") Long id,
			@RequestParam("connectionUsername") String username,
			@RequestParam(value = "connectionPassword", required = false) String password,
			@RequestParam("ip") String ip, @RequestParam("port") Integer port) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect c = new Connect();
		c.setUserId(user.getId());
		c.setId(id);
		Connect connect = connectDao.selectOne(c);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, FeedbackError.CHECK_FAIL_MSG, null);
		}
		Connect connect2 = new Connect();
		if (password == null) {
			connect2.setPassword(connect.getPassword());
		} else {
			connect2.setPassword(password);
		}
		connect2.setUsername(username);
		connect2.setHost(ip);
		connect2.setPort(port);
		BaseVO<Object> ret = testConnection(connect2);
		return ret;
	}

	private BaseVO<Object> testConnection(Connect conn) {
		try (Socket socket = new Socket();) {
			socket.connect(new InetSocketAddress(conn.getHost(), conn.getPort()), 5000);
		} catch (Exception e) {
			return new BaseVO<>(FeedbackError.TEST_CONN_FAIL, FeedbackError.TEST_CONN_FAIL_MSG, null);
		}
		Session session = null;
		try {
			session = new Session(conn.getHost(), conn.getPort(), conn.getUsername(), conn.getPassword());
			session.open();
		} catch (Exception e) {
			return new BaseVO<>(FeedbackError.TEST_CONN_FAIL_PWD, FeedbackError.TEST_CONN_FAIL_PWD_MSG, null);
		} finally {
			try {
				if (session != null) {
					session.close();
				}
			} catch (Exception e) {
				return new BaseVO<>(FeedbackError.TEST_CONN_FAIL_PWD, FeedbackError.TEST_CONN_FAIL_PWD_MSG, null);
			}
		}
		return BaseVO.success("Test Success", null);
	}

	@ApiOperation(value = "/api/connection/addThenReturnLess", notes = "/api/connection/addThenReturnLess")
	@RequestMapping(value = "/api/connection/addThenReturnLess", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionAddThenReturnLess(@RequestParam("connectionUsername") String username,
			@RequestParam("connectionPassword") String password, @RequestParam("ip") String ip,
			@RequestParam("port") Integer port, @RequestParam("connectionName") String connectionName) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect connect = new Connect();
		connect.setUser(user);
		connect.setAlias(connectionName);
		connect.setUsername(username);
		connect.setPassword(password);
		connect.setHost(ip);
		connect.setPort(port);
		connect.setCreateTime(Calendar.getInstance().getTime());

		int i = 0;
		try {
			i = transactionService.insertConnectTransactive(connect, user.getId());
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}

		if (i == 1) {
			dynamicSessionPool.addSessionPool(connect.getId(), ip, port, username, password,
					DynamicSessionPoolConfig.MAX_SIZE);

			ConnectCondition cc = new ConnectCondition();
			cc.setUserId(user.getId());
			cc.setLimiter(new PageParam(1, 10));
			cc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
			List<Connect> list = connectDao.selectAll(cc);
			Page<Connect> page = new Page<>(list, cc.getLimiter());
			return BaseVO.success("Add Connection Success", page);
		} else {
			return new BaseVO<>(FeedbackError.INSERT_CONN_FAIL, FeedbackError.INSERT_CONN_FAIL_MSG, null);
		}
	}

	@ApiOperation(value = "/api/connection/less", notes = "/api/connection/less")
	@RequestMapping(value = "/api/connection/less", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionLess() {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ConnectCondition cc = new ConnectCondition();
		cc.setUserId(user.getId());
		cc.setLimiter(new PageParam(1, 10));
		cc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
		List<Connect> list = connectDao.selectAll(cc);
		Page<Connect> page = new Page<>(list, cc.getLimiter());
		return BaseVO.success(page);
	}

	@ApiOperation(value = "/api/connection/all", notes = "/api/connection/all")
	@RequestMapping(value = "/api/connection/all", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionAll(@RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "aliasLike", required = false) String aliasLike) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ConnectCondition cc = new ConnectCondition();
		cc.setUserId(user.getId());
		cc.setAliasLike(aliasLike);
		cc.setLimiter(new PageParam(pageNum, pageSize));
		cc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
		List<Connect> list = connectDao.selectAll(cc);
		Page<Connect> page = new Page<>(list, cc.getLimiter());
		String defaultConnectId = user.getSetting() == null ? null : user.getSetting().getString("default_connect");
		return BaseVO.success(defaultConnectId, page);
	}

	@ApiOperation(value = "/api/connection/delete", notes = "/api/connection/delete")
	@RequestMapping(value = "/api/connection/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionDelete(@RequestParam("id") Long id) {
		Connect connect = connectDao.select(id);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.NO_CONN, FeedbackError.NO_CONN_MSG, null);
		}
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		if (connect.getUser() != null && user.getId().equals(connect.getUser().getId())) {
			int i = connectDao.delete(connect);
			if (i == 0) {
				return new BaseVO<>(FeedbackError.DELETE_CONN_FAIL, FeedbackError.DELETE_CONN_FAIL_MSG, null);
			} else {
				SessionPool sp = dynamicSessionPool.getSessionPool(connect.getId());
				dynamicSessionPool.removeSessionPool(connect.getId());
				sp.close();

				return BaseVO.success("Delete Connection Success", null);
			}
		} else {
			return new BaseVO<>(FeedbackError.USER_AUTH_FAIL, FeedbackError.USER_AUTH_FAIL_MSG, null);
		}
	}

	@ApiOperation(value = "/api/connection/update", notes = "/api/connection/update")
	@RequestMapping(value = "/api/connection/update", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionUpdate(@RequestParam("connectionUsername") String username,
			@RequestParam("connectionId") Long id, @RequestParam("connectionPassword") String password,
			@RequestParam("ip") String ip, @RequestParam("port") Integer port,
			@RequestParam("connectionName") String connectionName) {
		Connect connect = connectDao.select(id);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.NO_CONN, FeedbackError.NO_CONN_MSG, null);
		}
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		if (connect.getUser() != null && user.getId().equals(connect.getUser().getId())) {
			connect.setAlias(connectionName);
			connect.setUsername(username);
			connect.setPassword(password);
			connect.setHost(ip);
			connect.setPort(port);
			int i = 0;
			try {
				i = transactionService.updateAccountTransactive(connect, user.getId());
			} catch (BaseException e) {
				return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
			}
			if (i == 1) {
				dynamicSessionPool.addSessionPool(connect.getId(), connect.getHost(), connect.getPort(),
						connect.getUsername(), connect.getPassword(), DynamicSessionPoolConfig.MAX_SIZE);

				return BaseVO.success("Update Connection Success", null);
			} else {
				return new BaseVO<>(FeedbackError.INSERT_CONN_FAIL, FeedbackError.INSERT_CONN_FAIL_MSG, null);
			}
		} else {
			return new BaseVO<>(FeedbackError.USER_AUTH_FAIL, FeedbackError.USER_AUTH_FAIL_MSG, null);
		}
	}

	@ApiOperation(value = "/api/connection/view", notes = "/api/connection/view")
	@RequestMapping(value = "/api/connection/view", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionView(@RequestParam("connectionId") Long id) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect c = new Connect();
		c.setUserId(user.getId());
		c.setId(id);
		Connect connect = connectDao.selectOne(c);
		if (connect == null) {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, FeedbackError.CHECK_FAIL_MSG, null);
		} else {
			return BaseVO.success(connect);
		}
	}

	@ApiOperation(value = "/api/connection/default", notes = "/api/connection/default")
	@RequestMapping(value = "/api/connection/default", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionDefault(@RequestParam("connectionId") Long id) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		if (user != null) {
			JSONObject setting = user.getSetting();
			if (setting == null) {
				setting = new JSONObject();
			}
			setting.put("default_connect", id);
			user.setSetting(setting);
			user.setPassword(null);
			userDao.update(user);
		}
		return BaseVO.success(id);
	}

	@ApiOperation(value = "/api/connection/undefault", notes = "/api/connection/undefault")
	@RequestMapping(value = "/api/connection/undefault", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> connectionUndefault(@RequestParam("connectionId") Long id) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		if (user != null) {
			JSONObject setting = user.getSetting();
			if (setting == null) {
				setting = new JSONObject();
			}
			if (id.equals(setting.getLong("default_connect"))) {
				setting.remove("default_connect");
				user.setSetting(setting);
				user.setPassword(null);
				userDao.update(user);
			}
		}
		return BaseVO.success(null);
	}
}
