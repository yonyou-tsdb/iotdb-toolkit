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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.iotdb.ui.condition.EmailLogCondition;
import org.apache.iotdb.ui.config.EmailConfig;
import org.apache.iotdb.ui.config.shiro.UsernamePasswordIdToken;
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
import org.apache.iotdb.ui.util.MessageUtil;
import org.apache.iotdb.ui.util.VerifyCodeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
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

	@ApiOperation(value = "/api/login/account", notes = "/api/login/account")
	@RequestMapping(value = "/api/login/account", method = { RequestMethod.POST })
	public BaseVO<JSONObject> loginAccount(HttpServletRequest request, @RequestParam("username") String username,
			@RequestParam("password") String password) {
		JSONObject json = new JSONObject();
		User u = new User();
		u.setName(username);
		User user = userDao.selectOneWithEverything(u);
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
			ret = new BaseVO<JSONObject>(FeedbackError.ACCOUNT_LOGIN_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_LOGIN_ERROR), json);
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
	@RequestMapping(value = "/api/currentUser", method = { RequestMethod.POST })
	public BaseVO<JSONObject> currentUser() {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(USER);
		JSONObject json = new JSONObject();
		json.put("name", user.getName());
		return BaseVO.success(json);
	}

	@ApiOperation(value = "/api/outLogin", notes = "/api/outLogin")
	@RequestMapping(value = "/api/outLogin", method = { RequestMethod.POST })
	public BaseVO<JSONObject> outLogin() {
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		return BaseVO.success(null);
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/api/acquireCaptcha")
	public void acquireCaptcha(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "token") String token) {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		// 生成随机字串
		String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
		// 将token与verifyCode的组合存入缓存
		CaptchaWrapper cw = new CaptchaWrapper(verifyCode, System.currentTimeMillis() / 1000);
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

	private void sendRegisterEmail(String email, String username, String url) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("systemName", "Register");
		model.put("serviceName", "Activate account service");
		model.put("userName", username);
		model.put("activateAccountUrl", url);
		String[] emails = { email };
		sendEmail(model, "IoTDB-UI activate account service", "vm/register.vm", emails, emailConfig.getUsername());
	}

	@RequestMapping(value = "/api/register", method = { RequestMethod.POST })
	public BaseVO<JSONObject> register(HttpServletRequest request, @RequestParam(value = "mail") String mail,
			@RequestParam(value = "username") String username, @RequestParam(value = "password") String password,
			@RequestParam(value = "captcha") String captcha, @RequestParam(value = "token") String token) {

		// 查询captcha是否合格
		CaptchaWrapper cw = captchaMap.get(token);
		String realCaptcha = cw == null ? null : cw.getCaptchaValue();
		if (!captcha.equalsIgnoreCase(realCaptcha)) {
			return new BaseVO<JSONObject>(FeedbackError.ACCOUNT_CAPTCHA_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_CAPTCHA_ERROR), null);
		} else {
			captchaMap.remove(token);
		}
		// 每0.5秒内只能发送一次邮件
		Date now = LocalDateTime.now().toDate();
		Date timeLimitationAgo = new Date(now.getTime() - 500);

		EmailLogCondition elc = new EmailLogCondition();
		elc.setStatus(EmailLogStatus.INSERT);
		elc.setEmailTimeGreaterThan(timeLimitationAgo);
		elc.setEmailTimeLessOrEqual(now);
		int count = emailLogDao.count(elc);
		if (count > 0) {
			return new BaseVO<>(FeedbackError.ACCOUNT_EMAIL_ERROR, MessageUtil.get(FeedbackError.ACCOUNT_EMAIL_ERROR),
					null);
		}
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

		String url = String.format("http://%s/api/activateAccount/%s/%s", emailConfig.getEndPoint(), emailLog.getId(),
				randomToken);

		sendRegisterEmail(mail, username, url);

		return BaseVO.success(null);
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/api/activateAccount/{elId}/{token}")
	public void activateAccount(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("elId") Long elId, @PathVariable("token") String token) throws IOException {
		EmailLog emailLog = emailLogDao.select(elId);
		Date now = LocalDateTime.now().toDate();
		if (emailLog != null && token.equals(emailLog.getToken()) && emailLog.getAvailable()
				&& now.getTime() < emailLog.getDueTime().getTime()) {
			// 开始激活
			User user = new User();
			user.setName(emailLog.getTempAccount());
			user.setPassword(emailLog.getTempPassword());
			user.setSetting(new JSONObject());
			try {
				transactionService.insertUserTransactive(user, emailLog);
			} catch (BaseException e) {
				String url = String.format("http://%s/user/fail/?status=%s", emailConfig.getEndPoint(), e.getMessage());
				response.sendRedirect(url);
				return;
			}

			emailLog.setTempAccount(null);
			emailLog.setTempPassword(null);
			emailLog.setResetTime(now);
			emailLog.setAvailable(false);
			emailLog.setUserId(user.getId());
			emailLogDao.updatePersistent(emailLog);
			String url = String.format("http://%s/user/success/?status=%s", emailConfig.getEndPoint(),
					"Activate Account Success");
			response.sendRedirect(url);
		} else {
			String url = String.format("http://%s/user/fail/?status=%s", emailConfig.getEndPoint(),
					"Activate Account Fail, The Token Is Wrong Or Used Or Expired");
			response.sendRedirect(url);
		}
	}

	@RequestMapping(value = "/api/sendResetPasswordMail", method = { RequestMethod.POST })
	public BaseVO<JSONObject> sendResetPasswordMail(HttpServletRequest request,
			@RequestParam(value = "email") String email, @RequestParam(value = "captcha") String captcha,
			@RequestParam(value = "token") String token) {
		// 查询captcha是否合格
		CaptchaWrapper cw = captchaMap.get(token);
		String realCaptcha = cw == null ? null : cw.getCaptchaValue();
		if (!captcha.equalsIgnoreCase(realCaptcha)) {
			return new BaseVO<JSONObject>(FeedbackError.ACCOUNT_CAPTCHA_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_CAPTCHA_ERROR), null);
		}

		captchaMap.remove(token);
		// 每0.5秒内只能发送一次邮件
		Date now = LocalDateTime.now().toDate();
		Date timeLimitationAgo = new Date(now.getTime() - 500);

		EmailLogCondition elc = new EmailLogCondition();
		elc.setStatus(EmailLogStatus.UPDATE);
		elc.setEmailTimeGreaterThan(timeLimitationAgo);
		elc.setEmailTimeLessOrEqual(now);
		int count = emailLogDao.count(elc);
		if (count > 0) {
			return new BaseVO<>(FeedbackError.ACCOUNT_EMAIL_ERROR, MessageUtil.get(FeedbackError.ACCOUNT_EMAIL_ERROR),
					null);
		}

		// 通过邮箱查找用户
		EmailLog el = new EmailLog();
		el.setStatus(EmailLogStatus.INSERT);
		el.setAvailable(false);
		el.setEmail(email);
		EmailLog temp = emailLogDao.selectOne(el);
		if (temp == null || temp.getUser() == null) {
			return new BaseVO<>(FeedbackError.ACCOUNT_FIND_USER_BY_EMAIL_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_FIND_USER_BY_EMAIL_ERROR), null);
		}

		// 发送邮件
		EmailLog emailLog = new EmailLog();

		String randomToken = RandomGenerator.getRandomString(50);
		emailLog.setToken(randomToken);
		emailLog.setEmailTime(now);
		Date dueTime = new Date(now.getTime() + 86400000);
		emailLog.setDueTime(dueTime);
		emailLog.setEmail(email);
		emailLog.setAvailable(true);
		emailLog.setStatus(EmailLogStatus.UPDATE);
		emailLog.setTempAccount(temp.getUser().getName());

		emailLogDao.insert(emailLog);

		String url = String.format("http://%s/api/resetPassword/%s/%s", emailConfig.getEndPoint(), emailLog.getId(),
				randomToken);

		sendResetPasswordEmail(email, temp.getUser().getName(), url);
		return BaseVO.success(null);
	}

	private void sendResetPasswordEmail(String email, String username, String url) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("systemName", "Account");
		model.put("serviceName", "Reset password service");
		model.put("userName", username);
		model.put("url", url);
		String[] emails = { email };
		sendEmail(model, "IoTDB-UI reset password service", "vm/resetPassword.vm", emails, emailConfig.getUsername());
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/api/resetPassword/{elId}/{token}")
	public void resetPassword(HttpServletRequest request, HttpServletResponse response, @PathVariable("elId") Long elId,
			@PathVariable("token") String token) throws IOException {
		EmailLog emailLog = emailLogDao.select(elId);
		Date now = LocalDateTime.now().toDate();
		if (emailLog != null && token.equals(emailLog.getToken()) && emailLog.getAvailable()
				&& now.getTime() < emailLog.getDueTime().getTime()) {
			String url = String.format("http://%s/user/reset-password/?username=%s&id=%s&token=%s",
					emailConfig.getEndPoint(), emailLog.getTempAccount(), emailLog.getId(), token);
			response.sendRedirect(url);
		} else {
			String url = String.format("http://%s/user/fail/?status=%s", emailConfig.getEndPoint(),
					"Reset Password Fail, The Token Is Wrong Or Used Or Expired");
			response.sendRedirect(url);
		}
	}

	@RequestMapping(value = "/api/resetUpdatePassword", method = { RequestMethod.POST })
	public BaseVO<JSONObject> resetUpdatePassword(HttpServletRequest request, @RequestParam(value = "id") Long id,
			@RequestParam(value = "token") String token, @RequestParam(value = "password") String password) {
		EmailLog el = new EmailLog();
		el.setId(id);
		el.setToken(token);
		el.setStatus(EmailLogStatus.UPDATE);
		el.setAvailable(true);
		EmailLog emailLog = emailLogDao.selectOne(el);
		if (emailLog == null || emailLog.getTempAccount() == null) {
			return new BaseVO<>(FeedbackError.ACCOUNT_RESET_EMAILLOG_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_RESET_EMAILLOG_ERROR), null);
		}
		emailLog.setResetTime(LocalDateTime.now().toDate());
		emailLog.setAvailable(false);
		emailLogDao.update(emailLog);

		String encodedPassword = bCryptPasswordEncoder.encode(password);
		User u = new User();
		u.setName(emailLog.getTempAccount());
		User user = userDao.selectOne(u);
		if (user == null) {
			return new BaseVO<>(FeedbackError.ACCOUNT_RESET_UPDATE_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_RESET_UPDATE_ERROR), null);
		}
		user.setPassword(encodedPassword);
		int c = userDao.update(user);
		if (c != 1) {
			return new BaseVO<>(FeedbackError.ACCOUNT_RESET_UPDATE_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_RESET_UPDATE_ERROR), null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/updatePassword", method = { RequestMethod.POST })
	public BaseVO<JSONObject> updatePassword(@RequestParam(value = "passwordOrigin") String passwordOrigin,
			@RequestParam(value = "password") String password) {
		Subject subject = SecurityUtils.getSubject();
		User u = (User) subject.getSession().getAttribute(USER);
		User user = userDao.selectWithEverything(u.getId());
		if (user == null) {
			return new BaseVO<>(FeedbackError.GET_USER_FAIL, MessageUtil.get(FeedbackError.GET_USER_FAIL), null);
		}
		if ("user".equals(user.getName())) {
			return new BaseVO<>(FeedbackError.CHANGE_ACCOUNT_USER_PASSWORD_FAIL,
					MessageUtil.get(FeedbackError.CHANGE_ACCOUNT_USER_PASSWORD_FAIL), null);
		}
		if (!bCryptPasswordEncoder.matches(passwordOrigin, user.getPassword())) {
			return new BaseVO<>(FeedbackError.ACCOUNT_PASSWORD_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_PASSWORD_ERROR), null);
		}
		user.setPassword(bCryptPasswordEncoder.encode(password));
		int c = userDao.update(user);
		if (c != 1) {
			return new BaseVO<>(FeedbackError.ACCOUNT_RESET_UPDATE_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_RESET_UPDATE_ERROR), null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/deleteAccount", method = { RequestMethod.POST })
	public BaseVO<JSONObject> deleteAccount() {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(USER);
		if ("user".equals(user.getName())) {
			return new BaseVO<>(FeedbackError.DELETE_ACCOUNT_USER_FAIL,
					MessageUtil.get(FeedbackError.DELETE_ACCOUNT_USER_FAIL), null);
		}
		try {
			transactionService.deleteUserTransactive(user);
		} catch (BaseException e) {
			return new BaseVO<>(FeedbackError.ACCOUNT_DELETE_ERROR, MessageUtil.get(FeedbackError.ACCOUNT_DELETE_ERROR),
					null);
		}
		subject.logout();
		return BaseVO.success(null);
	}
}
