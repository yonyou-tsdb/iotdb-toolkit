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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.iotdb.ui.config.EmailConfig;
import org.apache.iotdb.ui.config.shiro.UsernamePasswordIdToken;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.UserDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.ThirdVelocityEmailService;
import org.apache.iotdb.ui.util.IpUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@Api(value = "Account API")
public class UserController {

	public static final String USER = "USER";

	@Autowired
	private UserDao userDao;

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private EmailConfig emailConfig;

	@Autowired
	private ThirdVelocityEmailService thirdVelocityEmailService;

	@ApiOperation(value = "user", notes = "user")
	@GetMapping(value = "/user")
	public BaseVO<User> user(@RequestParam("id") Long id) {
		User user = userDao.select(id);
		return BaseVO.success(user);
	}

	@ApiOperation(value = "connect", notes = "connect")
	@GetMapping(value = "/connect")
	public BaseVO<Connect> connect(@RequestParam("id") Long id) {
		Connect connect = connectDao.select(id);
		return BaseVO.success(connect);
	}

	@ApiOperation(value = "/api/login/account", notes = "/api/login/account")
	@RequestMapping(value = "/api/login/account", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<JSONObject> loginAccount(HttpServletRequest request, @RequestParam("username") String username,
			@RequestParam("password") String password) {
		JSONObject json = new JSONObject();
		if (username == null) {
			json.put("status", "error");
			json.put("type", "account");
			json.put("currentAuthority", "guest");
			return new BaseVO<JSONObject>("1", "用户名不能为空", json);
		}
		User u = new User();
		u.setName(username);
		User user = userDao.selectOne(u);
		BaseVO<JSONObject> ret = null;
		try {
			Subject subject = SecurityUtils.getSubject();
			UsernamePasswordIdToken token = new UsernamePasswordIdToken(username, password,
					String.valueOf(user.getId()), user.getPassword());
			user.setPassword(null);

			subject.login(token);
			Session session = subject.getSession();
			if (session != null) {
				session.setAttribute(USER, user);
			}
			json.put("status", "ok");
			json.put("type", "account");
			json.put("currentAuthority", "admin");
			json.put("jsessionid", IpUtils.getCookieValue(request, "JSESSIONID"));

			dealDefaultConnect(user, json);

			ret = BaseVO.success(json);
		} catch (Exception e) {
			json.put("status", "error");
			json.put("type", "account");
			json.put("currentAuthority", "guest");
			ret = new BaseVO<JSONObject>("1", "找不到用户或用户名与密码不匹配", json);
		}
		return ret;
	}

	private void dealDefaultConnect(User user, JSONObject json) {
		if (user.getSetting() != null && user.getSetting().getLong("default_connect") != null) {
			Connect c = new Connect();
			c.setId(user.getSetting().getLong("default_connect"));
			c.setUserId(user.getId());
			Connect defaultConnect = connectDao.selectOne(c);
			if (defaultConnect != null) {
				defaultConnect.setPassword(null);
				json.put("defaultConnect", defaultConnect);
			}
		}
	}

	@ApiOperation(value = "/api/currentUser", notes = "/api/currentUser")
	@RequestMapping(value = "/api/currentUser", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<JSONObject> currentUser() {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(USER);
		JSONObject json = new JSONObject();
		json.put("name", user.getName());
		return BaseVO.success(json);
	}

	@ApiOperation(value = "/api/outLogin", notes = "/api/outLogin")
	@RequestMapping(value = "/api/outLogin", method = { RequestMethod.GET, RequestMethod.POST })
	public JSONObject outLogin() {
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		JSONObject ret = new JSONObject();
		ret.put("data", new JSONObject());
		ret.put("success", true);
		return ret;
	}

	// 简单的无返回值的handler，无需写入swagger
	@RequestMapping(value = "/toLogin", method = { RequestMethod.GET, RequestMethod.POST })
	public void toLogin() {
	}

	private void sendEmail(Map<String, Object> model, String title, String vmPath, String[] emails, String from) {
		thirdVelocityEmailService.sendEmail(model, title, vmPath, emails, new String[] {}, from);
	}

	@RequestMapping(value = "/api/sendRegisterEmail", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<JSONObject> sendRegisterEmail(HttpServletRequest request,
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "email") String email) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("systemName", "Account");
		model.put("serviceName", "Activate account service");
		String userName = "asd";
		model.put("userName", userName);
		String url = "http://www.baidu.com";
		model.put("activateAccountUrl", url);
		String[] emails = { email };
		sendEmail(model, "IoTDB-UI Activate account service", "vm/register.vm", emails, emailConfig.getUsername());
		return BaseVO.success(null);
	}
}
