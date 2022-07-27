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
package org.apache.iotdb.ui.service;

import java.util.LinkedList;
import java.util.List;

import org.apache.iotdb.ui.condition.ConnectCondition;
import org.apache.iotdb.ui.condition.EmailLogCondition;
import org.apache.iotdb.ui.condition.QueryCondition;
import org.apache.iotdb.ui.entity.Board;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.EmailLog;
import org.apache.iotdb.ui.entity.Exporter;
import org.apache.iotdb.ui.entity.Panel;
import org.apache.iotdb.ui.entity.Query;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.BaseException;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.face.ConnectFace;
import org.apache.iotdb.ui.mapper.BoardDao;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.EmailLogDao;
import org.apache.iotdb.ui.mapper.ExporterDao;
import org.apache.iotdb.ui.mapper.PanelDao;
import org.apache.iotdb.ui.mapper.QueryDao;
import org.apache.iotdb.ui.mapper.UserDao;
import org.apache.iotdb.ui.model.EmailLogStatus;
import org.apache.iotdb.ui.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

	@Autowired
	private BoardDao boardDao;

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private ConnectService connectService;

	@Autowired
	private QueryDao queryDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PanelDao panelDao;

	@Autowired
	private EmailLogDao emailLogDao;

	@Autowired
	private ExporterDao exporterDao;

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int insertConnectTransactive(Connect connect, Long userId) throws BaseException {
		int ret = connectDao.insert(connect);
		Connect c = new Connect();
		c.setAlias(connect.getAlias());
		c.setUserId(userId);
		int n = connectDao.count(c);
		if (n != 1) {
			throw new BaseException(FeedbackError.ALIAS_REPEAT, MessageUtil.get(FeedbackError.ALIAS_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int updateAccountTransactive(Connect connect, Long userId) throws BaseException {
		int ret = connectDao.update(connect);
		Connect c = new Connect();
		c.setAlias(connect.getAlias());
		c.setUserId(userId);
		int n = connectDao.count(c);
		if (n != 1) {
			throw new BaseException(FeedbackError.ALIAS_REPEAT, MessageUtil.get(FeedbackError.ALIAS_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int insertQueryTransactive(Query query, Long connectId) throws BaseException {
		int ret = queryDao.insert(query);
		Query q = new Query();
		q.setName(query.getName());
		q.setConnectId(connectId);
		int n = queryDao.count(q);
		if (n != 1) {
			throw new BaseException(FeedbackError.QUERY_EXIST, MessageUtil.get(FeedbackError.QUERY_EXIST));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int insertEmailLogTransactive(EmailLog emailLog) throws BaseException {
		int ret = emailLogDao.insert(emailLog);
		EmailLogCondition elc = new EmailLogCondition();
		elc.setEmail(emailLog.getEmail());
		elc.setAvailable(false);
		elc.setStatus(EmailLogStatus.INSERT);
		int n = emailLogDao.count(elc);
		if (n > 0) {
			throw new BaseException(FeedbackError.ACCOUNT_REGISTER_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_REGISTER_ERROR));
		}
		User u = new User();
		u.setName(emailLog.getTempAccount());
		n = userDao.count(u);
		if (n > 0) {
			throw new BaseException(FeedbackError.ACCOUNT_REGISTER_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_REGISTER_ERROR));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int insertUserTransactive(User user, EmailLog emailLog) throws BaseException {
		int ret = userDao.insert(user);
		EmailLogCondition elc = new EmailLogCondition();
		elc.setEmailEqualOrUsernameEqual(emailLog.getEmail(), emailLog.getTempAccount());
		elc.setAvailable(false);
		elc.setStatus(EmailLogStatus.INSERT);
		int n = emailLogDao.count(elc);
		if (n > 0) {
			throw new BaseException(FeedbackError.ACCOUNT_ACTIVATE_ERROR,
					MessageUtil.get(FeedbackError.ACCOUNT_ACTIVATE_ERROR));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int deleteUserTransactive(User user) throws BaseException {
		connectService.loadUser(user, new Connect());
		QueryCondition qc = new QueryCondition();
		List<Long> connectIdList = new LinkedList<>();
		for (ConnectFace e : user.getConnect()) {
			connectIdList.add(((Connect) e).getId());
		}
		qc.setConnectIdIn(connectIdList);
		queryDao.delete(qc);

		ConnectCondition cc = new ConnectCondition();
		cc.setUserIdEqual(user.getId());
		connectDao.delete(cc);

		int ret = userDao.delete(user);

		try {
			EmailLogCondition elc = new EmailLogCondition();
			elc.setAccountIdEqual(user.getId());
			emailLogDao.delete(elc);
			elc.setAccountIdEqual(null);
			elc.setTempAccountEqual(user.getName());
			emailLogDao.delete(elc);
		} catch (Exception e) {
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int addExporterTransactive(Exporter exporter) throws BaseException {
		int ret = exporterDao.insert(exporter);
		Exporter e = new Exporter();
		e.setCode(exporter.getCode());
		int n = exporterDao.count(e);
		if (n != 1) {
			throw new BaseException(FeedbackError.EXPORTER_CODE_REPEAT,
					MessageUtil.get(FeedbackError.EXPORTER_CODE_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int editExporterTransactive(Exporter exporter) throws BaseException {
		int ret = exporterDao.update(exporter);
		Exporter e = new Exporter();
		e.setCode(exporter.getCode());
		int n = exporterDao.count(e);
		if (n != 1) {
			throw new BaseException(FeedbackError.EXPORTER_CODE_REPEAT,
					MessageUtil.get(FeedbackError.EXPORTER_CODE_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int addBoardTransactive(Board board) throws BaseException {
		int ret = boardDao.insert(board);
		if (board.getToken() == null) {
			return ret;
		}
		Board b = new Board();
		b.setToken(board.getToken());
		int n = boardDao.count(b);
		if (n != 1) {
			throw new BaseException(FeedbackError.BOARD_TOKEN_REPEAT,
					MessageUtil.get(FeedbackError.BOARD_TOKEN_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int editBoardTransactive(Board board) throws BaseException {
		int ret = boardDao.update(board);
		if (board.getToken() == null) {
			return ret;
		}
		Board e = new Board();
		e.setToken(board.getToken());
		int n = boardDao.count(e);
		if (n != 1) {
			throw new BaseException(FeedbackError.BOARD_TOKEN_REPEAT,
					MessageUtil.get(FeedbackError.BOARD_TOKEN_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int addPanelTransactive(Panel panel) throws BaseException {
		int ret = panelDao.insert(panel);
		Panel p = new Panel();
		p.setBoardId(panel.getBoardId());
		p.setName(panel.getName());
		int n = panelDao.count(p);
		if (n != 1) {
			throw new BaseException(FeedbackError.PANEL_NAME_REPEAT, MessageUtil.get(FeedbackError.PANEL_NAME_REPEAT));
		}
		return ret;
	}

	@Transactional(value = "transactionManager1", rollbackFor = {
			BaseException.class }, readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public int editPanelTransactive(Panel panel) throws BaseException {
		int ret = panelDao.update(panel);
		Panel p = new Panel();
		p.setBoardId(panel.getBoardId());
		p.setName(panel.getName());
		int n = panelDao.count(p);
		if (n != 1) {
			throw new BaseException(FeedbackError.PANEL_NAME_REPEAT, MessageUtil.get(FeedbackError.PANEL_NAME_REPEAT));
		}
		return ret;
	}
}
