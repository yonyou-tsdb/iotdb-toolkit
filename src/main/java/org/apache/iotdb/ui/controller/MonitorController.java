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
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.ui.condition.BoardCondition;
import org.apache.iotdb.ui.condition.ExporterCondition;
import org.apache.iotdb.ui.condition.PanelCondition;
import org.apache.iotdb.ui.config.MonitorServerConfig;
import org.apache.iotdb.ui.config.schedule.ExporterTimerBucket;
import org.apache.iotdb.ui.entity.Board;
import org.apache.iotdb.ui.entity.Exporter;
import org.apache.iotdb.ui.entity.Panel;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.BaseException;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.BoardDao;
import org.apache.iotdb.ui.mapper.ExporterDao;
import org.apache.iotdb.ui.mapper.PanelDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.PanelService;
import org.apache.iotdb.ui.service.TransactionService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.joda.time.LocalDateTime;
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
	private BoardDao boardDao;

	@Autowired
	private ExporterDao exporterDao;

	@Autowired
	private ExporterTimerBucket exporterTimerBucket;

	@Autowired
	@Qualifier("httpClientBean")
	private CloseableHttpClient HttpClientBean;

	@Autowired
	private MonitorServerConfig monitorServerConfig;

	@Autowired
	private PanelDao panelDao;

	@Autowired
	private PanelService panelService;

	@Autowired
	private QueryController queryController;

	@Autowired
	private TransactionService transactionService;

	private static final int MONITOR_DATA_PAGE_SIZE = 1000;

	@RequestMapping(value = "/api/monitor/exporter/all", method = { RequestMethod.POST })
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

	@RequestMapping(value = "/api/monitor/exporter/view", method = { RequestMethod.POST })
	public BaseVO<Object> exporterView(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		ExporterCondition ec = new ExporterCondition();
		ec.setId(id);
		ec.setUserId(user.getId());
		Exporter exporter = exporterDao.selectOne(ec);
		if (exporter == null) {
			return new BaseVO<>(FeedbackError.EXPORTER_GET_FAIL, null);
		} else {
			return BaseVO.success(exporter);
		}
	}

	@RequestMapping(value = "/api/monitor/exporter/update", method = { RequestMethod.POST })
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
			return new BaseVO<>(FeedbackError.EXPORTER_GET_FAIL, null);
		}
		exporter.setName(name);
		exporter.setEndPoint(endpoint);
		exporter.setPeriod(period);
		exporter.setCode(code);
		exporter.setUpdateTime(LocalDateTime.now().toDate());
		try {
			transactionService.editExporterTransactive(exporter);
			exporterTimerBucket.addExporterTimer(exporter);
			return BaseVO.success(name, exporter);
		} catch (BaseException e2) {
			return new BaseVO<>(e2.getErrorCode(), e2.getMessage(), null);
		}
	}

	@RequestMapping(value = "/api/monitor/exporter/delete", method = { RequestMethod.POST })
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
			return new BaseVO<>(FeedbackError.EXPORTER_DELETE_FAIL, null);
		}
	}

	@RequestMapping(value = "/api/monitor/exporter/add", method = { RequestMethod.POST })
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
		Date now = LocalDateTime.now().toDate();
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

	@RequestMapping(value = "/api/monitor/board/view", method = { RequestMethod.POST })
	public BaseVO<Object> boardView(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		BoardCondition bc = new BoardCondition();
		bc.setId(id);
		bc.setUserId(user.getId());
		Board board = boardDao.selectOne(bc);
		if (board == null) {
			return new BaseVO<>(FeedbackError.BOARD_GET_FAIL, null);
		} else {
			return BaseVO.success(board);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/api/monitor/board/viewMore", method = { RequestMethod.POST })
	public BaseVO<Object> boardViewMore(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam(value = "panelNameLike", required = false) String panelNameLike) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		BoardCondition bc = new BoardCondition();
		bc.setId(id);
		bc.setUserId(user.getId());
		Board board = boardDao.selectOne(bc);
		if (board == null) {
			return new BaseVO<>(FeedbackError.BOARD_GET_FAIL, null);
		} else {
			PanelCondition m = new PanelCondition();
			m.setNameLike(panelNameLike);
			m.setSorter(new SortParam(new Order("display_order", Conditionable.Sequence.ASC),
					new Order("tb_panel_0.id", Conditionable.Sequence.DESC)));
			m.setUserId(user.getId());
			panelService.loadBoard(board, m);
			for (Panel e : (Collection<Panel>) board.getPanel()) {
				loadPanelDataList(e);
			}
			return BaseVO.success(board);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/api/anonymous/monitor/board/viewMore", method = { RequestMethod.POST })
	public BaseVO<Object> boardViewMoreAnonymous(HttpServletRequest request, @RequestParam("token") String token,
			@RequestParam(value = "panelNameLike", required = false) String panelNameLike) throws SQLException {
		BoardCondition bc = new BoardCondition();
		bc.setToken(token);
		Board board = boardDao.selectOne(bc);
		if (board == null) {
			return new BaseVO<>(FeedbackError.BOARD_GET_FAIL, null);
		} else {
			PanelCondition m = new PanelCondition();
			m.setNameLike(panelNameLike);
			m.setSorter(new SortParam(new Order("display_order", Conditionable.Sequence.ASC),
					new Order("tb_panel_0.id", Conditionable.Sequence.DESC)));
			panelService.loadBoard(board, m);
			for (Panel e : (Collection<Panel>) board.getPanel()) {
				loadPanelDataList(e);
			}
			return BaseVO.success(board);
		}
	}

	private void loadPanelDataList(Panel panel) {
		Session innerSession = new Session(monitorServerConfig.getHost(), 6667, "root", "root");
		try {
			innerSession.open(false, 60_000);
			SessionDataSet ds = innerSession.executeQueryStatement(panel.getQuery());
			panel.setMonitorDataList(new LinkedList<>());
			queryController.transformForQuery(panel.getMonitorDataList(), ds, MONITOR_DATA_PAGE_SIZE);
		} catch (IoTDBConnectionException | StatementExecutionException e) {
		}
	}

	@RequestMapping(value = "/api/monitor/panel/reload", method = { RequestMethod.POST })
	public BaseVO<Object> panelReload(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		PanelCondition mc = new PanelCondition();
		mc.setId(id);
		mc.setUserId(user.getId());
		Panel panel = panelDao.selectOne(mc);
		if (panel == null) {
			return new BaseVO<>(FeedbackError.PANEL_GET_FAIL, null);
		} else {
			loadPanelDataList(panel);
			return BaseVO.success(panel);
		}
	}

	@RequestMapping(value = "/api/anonymous/monitor/panel/reload", method = { RequestMethod.POST })
	public BaseVO<Object> panelReloadAnonymous(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam("token") String token) throws SQLException {
		PanelCondition pc = new PanelCondition();
		pc.setId(id);
		Board b = new Board();
		b.setToken(token);
		pc.setBoard(b);
		Panel panel = panelDao.selectOne(pc);
		if (panel == null) {
			return new BaseVO<>(FeedbackError.PANEL_GET_FAIL, null);
		} else {
			loadPanelDataList(panel);
			return BaseVO.success(panel);
		}
	}

	@RequestMapping(value = "/api/monitor/board/all", method = { RequestMethod.POST })
	public BaseVO<Object> boardAll(HttpServletRequest request, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "nameLike", required = false) String nameLike) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		BoardCondition bc = new BoardCondition();
		bc.setUserId(user.getId());
		bc.setNameLike(nameLike);
		bc.setLimiter(new PageParam(pageNum, pageSize));
		bc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
		List<Board> list = boardDao.selectAll(bc);
		Page<Board> page = new Page<>(list, bc.getLimiter());
		return BaseVO.success(page);
	}

	@RequestMapping(value = "/api/monitor/board/add", method = { RequestMethod.POST })
	public BaseVO<Object> boardAdd(HttpServletRequest request, @RequestParam("name") String name,
			@RequestParam(required = false, value = "token") String token) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Board board = new Board();
		board.setName(name);
		board.setToken(token);
		board.setUserId(user.getId());
		Date now = LocalDateTime.now().toDate();
		board.setCreateTime(now);
		board.setUpdateTime(now);
		try {
			transactionService.addBoardTransactive(board);
			return BaseVO.success(name, null);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/api/monitor/board/delete", method = { RequestMethod.POST })
	public BaseVO<Object> boardDelete(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		BoardCondition b = new BoardCondition();
		b.setUserIdEqual(user.getId());
		b.setIdEqual(id);
		int i = boardDao.delete(b);
		if (i == 1) {
			PanelCondition mc = new PanelCondition();
			mc.setBoardIdEqual(id);
			panelDao.delete(mc);
			return BaseVO.success(null);
		} else {
			return new BaseVO<>(FeedbackError.BOARD_DELETE_FAIL, null);
		}
	}

	@RequestMapping(value = "/api/monitor/board/update", method = { RequestMethod.POST })
	public BaseVO<Object> boardUpdate(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam("name") String name, @RequestParam(value = "token", required = false) String token)
			throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		BoardCondition bc = new BoardCondition();
		bc.setUserId(user.getId());
		bc.setId(id);
		Board board = boardDao.selectOne(bc);

		if (board == null) {
			return new BaseVO<>(FeedbackError.BOARD_GET_FAIL, null);
		}
		board.setName(name);
		board.setToken(token);
		board.setUpdateTime(LocalDateTime.now().toDate());
		try {
			transactionService.editBoardTransactive(board);
			return BaseVO.success(name, board);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/api/monitor/panel/all", method = { RequestMethod.POST })
	public BaseVO<Object> panelAll(HttpServletRequest request, @RequestParam("boardId") Long boardId,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "nameLike", required = false) String nameLike) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		PanelCondition mc = new PanelCondition();
		mc.setUserId(user.getId());
		mc.setBoardId(boardId);
		mc.setNameLike(nameLike);
		mc.setLimiter(new PageParam(pageNum, pageSize));
		mc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
		List<Panel> list = panelDao.selectAll(mc);
		Page<Panel> page = new Page<>(list, mc.getLimiter());
		return BaseVO.success(page);
	}

	@RequestMapping(value = "/api/monitor/panel/add", method = { RequestMethod.POST })
	public BaseVO<Object> panelAdd(HttpServletRequest request, @RequestParam("boardId") Long boardId,
			@RequestParam("name") String name, @RequestParam("query") String query,
			@RequestParam("period") Integer period, @RequestParam(value = "displayOrder") Integer displayOrder)
			throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Panel p = new Panel();
		p.setBoardId(boardId);
		int c = panelDao.count(p);
		if (c > 20) {
			return new BaseVO<>(FeedbackError.PANEL_REACH_LIMIT, null);
		}
		Panel panel = new Panel();
		panel.setUserId(user.getId());
		panel.setBoardId(boardId);
		panel.setName(name);
		panel.setQuery(query);
		panel.setPeriod(period);
		panel.setDisplayOrder(displayOrder);
		Date now = LocalDateTime.now().toDate();
		panel.setCreateTime(now);
		panel.setUpdateTime(now);
		try {
			transactionService.addPanelTransactive(panel);
			return BaseVO.success(name, null);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/api/monitor/panel/update", method = { RequestMethod.POST })
	public BaseVO<Object> panelUpdate(HttpServletRequest request, @RequestParam("id") Long id,
			@RequestParam("name") String name, @RequestParam("query") String query,
			@RequestParam("period") Integer period, @RequestParam(value = "displayOrder") Integer displayOrder)
			throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		PanelCondition pc = new PanelCondition();
		pc.setUserId(user.getId());
		pc.setId(id);
		Panel panel = panelDao.selectOne(pc);

		if (panel == null) {
			return new BaseVO<>(FeedbackError.PANEL_GET_FAIL, null);
		}
		panel.setName(name);
		panel.setQuery(query);
		panel.setPeriod(period);
		panel.setDisplayOrder(displayOrder);
		panel.setUpdateTime(LocalDateTime.now().toDate());
		try {
			transactionService.editPanelTransactive(panel);
			return BaseVO.success(name, panel);
		} catch (BaseException e2) {
			return new BaseVO<>(e2.getErrorCode(), e2.getMessage(), null);
		}
	}

	@RequestMapping(value = "/api/monitor/panel/delete", method = { RequestMethod.POST })
	public BaseVO<Object> panelDelete(HttpServletRequest request, @RequestParam("id") Long id) throws SQLException {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		PanelCondition p = new PanelCondition();
		p.setUserIdEqual(user.getId());
		p.setIdEqual(id);
		int i = panelDao.delete(p);
		if (i == 1) {
			return BaseVO.success(null);
		} else {
			return new BaseVO<>(FeedbackError.PANEL_DELETE_FAIL, null);
		}
	}
}
