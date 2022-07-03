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

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.iotdb.ui.condition.ExporterCondition;
import org.apache.iotdb.ui.config.schedule.ExporterTimerBucket;
import org.apache.iotdb.ui.entity.Exporter;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.BaseException;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ExporterDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.TransactionService;
import org.apache.iotdb.ui.util.MessageUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Api(value = "Monitor API")
public class MonitorController {

	@Autowired
	private ExporterDao exporterDao;

	@Autowired
	private ExporterTimerBucket exporterTimerBucket;

	@Autowired
	@Qualifier("httpClientBean")
	private CloseableHttpClient HttpClientBean;

	@Autowired
	private TransactionService transactionService;

	@RequestMapping(value = "/api/monitor/exporter/all", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exporterAll(HttpServletRequest request, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "nameLike", required = false) String nameLike) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ExporterCondition ec = new ExporterCondition();
		ec.setUserId(user.getId());
		ec.setNameLike(nameLike);
		ec.setLimiter(new PageParam(pageNum, pageSize));
		ec.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
		List<Exporter> list = exporterDao.selectAll(ec);
		Page<Exporter> page = new Page<>(list, ec.getLimiter());
		return BaseVO.success(page);
	}

	@RequestMapping(value = "/api/monitor/exporter/view", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exporterView(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ExporterCondition ec = new ExporterCondition();
		ec.setId(id);
		ec.setUserId(user.getId());
		Exporter exporter = exporterDao.selectOne(ec);
		if (exporter == null) {
			return new BaseVO<>(FeedbackError.EXPORTER_GET_FAIL, MessageUtil.get(FeedbackError.EXPORTER_GET_FAIL),
					null);
		} else {
			return BaseVO.success(exporter);
		}
	}

	@RequestMapping(value = "/api/monitor/exporter/update", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exporterUpdate(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam("name") String name, @RequestParam("endpoint") String endpoint,
			@RequestParam("period") Integer period, @RequestParam("code") String code) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ExporterCondition ec = new ExporterCondition();
		ec.setUserId(user.getId());
		ec.setId(id);
		Exporter exporter = exporterDao.selectOne(ec);

		if (exporter == null) {
			return new BaseVO<>(FeedbackError.EXPORTER_GET_FAIL, MessageUtil.get(FeedbackError.EXPORTER_GET_FAIL),
					null);
		}
		exporter.setName(name);
		exporter.setEndPoint(endpoint);
		exporter.setPeriod(period);
		exporter.setCode(code);
		exporter.setUpdateTime(Calendar.getInstance().getTime());
		try {
			transactionService.editExporterTransactive(exporter);
			exporterTimerBucket.addExporterTimer(exporter);
			return BaseVO.success(name, exporter);
		} catch (BaseException e2) {
			return new BaseVO<>(e2.getErrorCode(), e2.getMessage(), null);
		}
	}

	@RequestMapping(value = "/api/monitor/exporter/delete", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exporterDelete(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ExporterCondition e = new ExporterCondition();
		e.setUserIdEqual(user.getId());
		e.setIdEqual(id);
		int i = exporterDao.delete(e);
		if (i == 1) {
			exporterTimerBucket.removeExporterTimer(id);
			return BaseVO.success(null);
		} else {
			return new BaseVO<>(FeedbackError.EXPORTER_DELETE_FAIL, MessageUtil.get(FeedbackError.EXPORTER_DELETE_FAIL),
					null);
		}
	}

	@RequestMapping(value = "/api/monitor/exporter/add", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exporterAdd(HttpServletRequest request, @RequestParam("name") String name,
			@RequestParam("endpoint") String endpoint, @RequestParam("period") Integer period,
			@RequestParam("code") String code) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Exporter exporter = new Exporter();
		exporter.setName(name);
		exporter.setEndPoint(endpoint);
		exporter.setPeriod(period);
		exporter.setCode(code);
		Date now = Calendar.getInstance().getTime();
		exporter.setCreateTime(now);
		exporter.setUpdateTime(now);
		exporter.setUserId(user.getId());
		try {
			transactionService.addExporterTransactive(exporter);
			exporterTimerBucket.addExporterTimer(exporter);
			return BaseVO.success(name, null);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
	}
}
