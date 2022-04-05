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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.iotdb.ui.config.EmailConfig;
import org.apache.iotdb.ui.config.shiro.UsernamePasswordIdToken;
import org.apache.iotdb.ui.config.websocket.TimerConfig;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.EmailLog;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.BaseException;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.EmailLogDao;
import org.apache.iotdb.ui.mapper.UserDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.model.CaptchaWrapper;
import org.apache.iotdb.ui.model.EmailLogStatus;
import org.apache.iotdb.ui.service.RandomGenerator;
import org.apache.iotdb.ui.service.ThirdVelocityEmailService;
import org.apache.iotdb.ui.service.TransactionService;
import org.apache.iotdb.ui.util.IpUtils;
import org.apache.iotdb.ui.util.VerifyCodeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	public static Map<String, CaptchaWrapper> captchaMap = new ConcurrentHashMap<>();

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private EmailConfig emailConfig;

	@Autowired
	private EmailLogDao emailLogDao;

	@Autowired
	private ThirdVelocityEmailService thirdVelocityEmailService;

	@Autowired
	private TransactionService transactionService;

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

	@RequestMapping(method = { RequestMethod.GET }, value = "/api/acquireCaptcha")
	public void sendRegisterEmail(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "token") String token) {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		// 生成随机字串
		String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
		// 将token与verifyCode的组合存入缓存
		CaptchaWrapper cw = new CaptchaWrapper(verifyCode, TimerConfig.cou);
		captchaMap.put(token, cw);
		// 生成图片
		int w = 100, h = 30;
		try {
			VerifyCodeUtils.outputImage(w, h, response.getOutputStream(), verifyCode);
		} catch (IOException e) {
		}
	}

	private void sendEmail(Map<String, Object> model, String title, String vmPath, String[] emails, String from) {
		thirdVelocityEmailService.sendEmail(model, title, vmPath, emails, new String[] {}, from);
	}

//	@RequestMapping(value = "/api/sendRegisterEmail", method = { RequestMethod.GET, RequestMethod.POST })
//	public BaseVO<JSONObject> sendRegisterEmail(HttpServletRequest request,
//			@RequestParam(value = "username", required = false) String username,
//			@RequestParam(value = "password", required = false) String password,
//			@RequestParam(value = "email") String email) {
//		sendRegisterEmail(email);
//		return BaseVO.success(null);
//	}

	private void sendRegisterEmail(String email, String username, String url) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("systemName", "Register");
		model.put("serviceName", "Activate account service");
		model.put("userName", username);
		model.put("activateAccountUrl", url);
		String[] emails = { email };
		sendEmail(model, "IoTDB-UI Activate account service", "vm/register.vm", emails, emailConfig.getUsername());
	}

	@RequestMapping(value = "/api/register", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<JSONObject> register(HttpServletRequest request, @RequestParam(value = "mail") String mail,
			@RequestParam(value = "username") String username, @RequestParam(value = "password") String password,
			@RequestParam(value = "captcha") String captcha, @RequestParam(value = "token") String token) {

		// 查询captcha是否合格
		CaptchaWrapper cw = captchaMap.get(token);
		String realCaptcha = cw == null ? null : cw.getCaptchaValue();
		if (!captcha.equalsIgnoreCase(realCaptcha)) {
			return new BaseVO<JSONObject>(FeedbackError.ACCOUNT_CAPTCHA_ERROR, FeedbackError.ACCOUNT_CAPTCHA_ERROR_MSG,
					null);
		} else {
			captchaMap.remove(token);
		}
		// 查询是否在1分钟之内已发送过邮件
		Date now = Calendar.getInstance().getTime();
		Date timeLimitationAgo = new Date(now.getTime() - 500);
		// 发送邮件
		EmailLog emailLog = new EmailLog();

		String randomToken = RandomGenerator.getRandomString(50);
		emailLog.setToken(randomToken);
		emailLog.setEmailTime(now);
		Date dueTime = new Date(now.getTime() + 86400000);
		emailLog.setDueTime(dueTime);
		emailLog.setEmail(mail);
		emailLog.setAvailable(true);
		emailLog.setStatus(EmailLogStatus.INSERT);
		emailLog.setTempAccount(username);
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		emailLog.setTempPassword(encodedPassword);

		try {
			transactionService.insertEmailLogTransactive(emailLog);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}

		String url = "http://localhost:8080" + "/api/activateAccount/" + emailLog.getId() + "/" + randomToken;

		sendRegisterEmail(mail, username, url);

		return BaseVO.success(null);
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/api/activateAccount/{elId}/{token}")
	public BaseVO<JSONObject> activateAccount(@PathVariable("elId") Long elId, @PathVariable("token") String token) {
		EmailLog emailLog = emailLogDao.select(elId);
		if (emailLog != null && token.equals(emailLog.getToken()) && emailLog.getAvailable()) {
			// 开始激活
			User user = new User();
			user.setName(emailLog.getTempAccount());
			user.setPassword(emailLog.getTempPassword());
			user.setSetting(new JSONObject());
			userDao.insert(user);

			emailLog.setTempAccount(null);
			emailLog.setTempPassword(null);
			Date now = Calendar.getInstance().getTime();
			emailLog.setResetTime(now);
			emailLog.setAvailable(false);
			emailLog.setUserId(user.getId());
			emailLogDao.updatePersistent(emailLog);
			return BaseVO.success("Activate account success.", null);
		} else {
			return BaseVO.success("Activate account fail, the token is wrong or used or expired.", null);
		}
	}
}
