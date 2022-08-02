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
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.session.pool.SessionDataSetWrapper;
import org.apache.iotdb.tsfile.read.common.Field;
import org.apache.iotdb.tsfile.read.common.RowRecord;
import org.apache.iotdb.ui.condition.QueryCondition;
import org.apache.iotdb.ui.config.ContinuousIoTDBSession;
import org.apache.iotdb.ui.config.tsdatasource.DynamicDataSourceAspect;
import org.apache.iotdb.ui.config.tsdatasource.DynamicSessionPool;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.Query;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.exception.BaseException;
import org.apache.iotdb.ui.exception.FeedbackError;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.QueryDao;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.model.CompressMode;
import org.apache.iotdb.ui.model.ExportTimeFormat;
import org.apache.iotdb.ui.service.TransactionService;
import org.apache.iotdb.ui.util.IpUtils;
import org.apache.iotdb.ui.util.MessageUtil;
import org.apache.iotdb.ui.util.SessionExportCsv;
import org.apache.iotdb.ui.util.SessionImportCsv;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.pagination.Order;
import indi.mybatis.flying.pagination.Page;
import indi.mybatis.flying.pagination.PageParam;
import indi.mybatis.flying.pagination.SortParam;
import io.swagger.annotations.Api;

@CrossOrigin
@RestController
@Api(value = "Query Management API")
public class QueryController {

	@Autowired
	@Qualifier("dynamicSessionPool")
	private DynamicSessionPool dynamicSessionPool;

	@Autowired
	private IotDBController iotDBController;

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private QueryDao queryDao;

	@Autowired
	private TransactionService transactionService;

	protected static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

	private static final int PAGE_SIZE = 5000;

	private static final int BATCH_SIZE = 100_000;

	public Session getDetermineTemporarySession() {
		return dynamicSessionPool.determineTemporarySession();
	}

	private boolean checkConnectAuth(Long connectId) throws BaseException {
		if (connectId == null) {
			throw new BaseException(FeedbackError.SELECT_CONNECTION_FAIL,
					MessageUtil.get(FeedbackError.SELECT_CONNECTION_FAIL));
		}
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getSession().getAttribute(UserController.USER);
		Connect c = new Connect();
		c.setUserId(user.getId());
		c.setId(connectId);
		int n = connectDao.count(c);
		return n > 0;
	}

	public List<Map<String, Object>> transform(SessionDataSetWrapper sessionDataSetWrapper, boolean debug) {
		List<Map<String, Object>> list = new LinkedList<>();
		try {
			List<String> columnNames = sessionDataSetWrapper.getColumnNames();
			boolean hasTime = false;
			if (columnNames.contains("Time")) {
				hasTime = true;
				columnNames.remove("Time");
			}
			while (sessionDataSetWrapper.hasNext()) {
				RowRecord rowRecord = sessionDataSetWrapper.next();
				if (debug) {
					LOGGER.error(JSONObject.toJSONString(rowRecord.getFields()));
				}
				Map<String, Object> map = new LinkedHashMap<>();
				if (hasTime) {
					map.put("Time", rowRecord.getTimestamp());
				}
				Iterator<String> it = columnNames.iterator();
				int j = 0;
				while (it.hasNext()) {
					String next = it.next();
					Field f = rowRecord.getFields().get(j);
					map.put(next, f.getDataType() == null ? null : f.getStringValue());
					j++;
				}
				list.add(map);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			iotDBController.getDetermineSessionPool().closeResultSet(sessionDataSetWrapper);
		}
		return list;
	}

	public List<Map<String, Object>> transform(SessionDataSetWrapper sessionDataSetWrapper) {
		return transform(sessionDataSetWrapper, false);
	}

	public boolean transformForQuery(List<Map<String, Object>> list, SessionDataSet sessionDataSet, int rows,
			boolean debug) {
		boolean ret = false;
		try {
			List<String> columnNames = sessionDataSet.getColumnNames();
			boolean hasTime = false;
			if (columnNames.contains("Time")) {
				hasTime = true;
				columnNames.remove("Time");
			}
			int columns = columnNames.size();
			if (columns < 1) {
				columns = 1;
			}
			rows = Math.min(BATCH_SIZE / columns, rows);
			int i = 0;
			while (sessionDataSet.hasNext()) {
				i++;
				RowRecord rowRecord = sessionDataSet.next();
				if (debug) {
					LOGGER.error(rowRecord.getFields().toString());
				}
				Map<String, Object> map = new LinkedHashMap<>();
				if (hasTime) {
					map.put("Time", rowRecord.getTimestamp());
				}
				Iterator<String> it = columnNames.iterator();
				int j = 0;
				while (it.hasNext()) {
					String next = it.next();
					Field f = rowRecord.getFields().get(j);
					map.put(next, f.getDataType() == null ? null : f.getStringValue());
					j++;
				}
				list.add(map);
				if (i == rows) {
					ret = true;
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return ret;
	}

	public boolean transform(List<Map<String, Object>> list, SessionDataSet sessionDataSet, int rows, boolean debug) {
		boolean ret = false;
		try {
			List<String> columnNames = sessionDataSet.getColumnNames();
			boolean hasTime = false;
			if (columnNames.contains("Time")) {
				hasTime = true;
				columnNames.remove("Time");
			}
			int i = 0;
			while (sessionDataSet.hasNext()) {
				i++;
				RowRecord rowRecord = sessionDataSet.next();
				if (debug) {
					LOGGER.error(rowRecord.getFields().toString());
				}
				Map<String, Object> map = new LinkedHashMap<>();
				if (hasTime) {
					map.put("Time", rowRecord.getTimestamp());
				}
				Iterator<String> it = columnNames.iterator();

				int j = 0;
				while (it.hasNext()) {
					String next = it.next();
					Field f = rowRecord.getFields().get(j);
					map.put(next, f.getDataType() == null ? null : f.getStringValue());
					j++;
				}
				list.add(map);
				if (i == rows) {
					ret = true;
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return ret;
	}

	public boolean transform(List<Map<String, Object>> list, SessionDataSet sessionDataSet, int rows) {
		return transform(list, sessionDataSet, rows, false);
	}

	public boolean transformForQuery(List<Map<String, Object>> list, SessionDataSet sessionDataSet, int rows) {
		return transformForQuery(list, sessionDataSet, rows, false);
	}

	@RequestMapping(value = "/api/query/querySql", method = { RequestMethod.POST })
	public BaseVO<Object> querySqlWithTenant(HttpServletRequest request, @RequestParam(value = "sqls") String sqls,
			@RequestParam(value = "queryToken") String queryToken, @RequestParam(value = "tabKey") String tabKey,
			@RequestParam(value = "tabToken") String tabToken) throws SQLException {
		Long connectId = DynamicDataSourceAspect.getTenantCode(request);
		boolean b = false;
		try {
			b = checkConnectAuth(connectId);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
		if (b) {
			try {
				sqls = sqls.trim();
				Pattern p = Pattern.compile("(?ms)('(?:''|[^'])*')|--.*?$|//.*?$|/\\*.*?\\*/|#.*?$|");
				sqls = p.matcher(sqls).replaceAll("$1");

				String lowerSql = sqls.toLowerCase();
				if ("".equals(lowerSql)) {
					return BaseVO.success("0", Collections.EMPTY_LIST);
				}
				if (lowerSql.startsWith("tracing") || lowerSql.startsWith("select") || lowerSql.startsWith("show")
						|| lowerSql.startsWith("list") || lowerSql.startsWith("count")
						|| lowerSql.startsWith("debug")) {
					Session session = getDetermineTemporarySession();
					session.open(false, 70_000);

					Long before = System.currentTimeMillis();

					SessionDataSet ds = session.executeQueryStatement(sqls);

					List<Map<String, Object>> list = new LinkedList<>();
					boolean hasMore = transformForQuery(list, ds, PAGE_SIZE);
					Long after = System.currentTimeMillis();

					if (hasMore) {
						ContinuousIoTDBSession.addContinuousDataSet(queryToken, ds);
					}

					JSONObject json = new JSONObject();
					json.put("costMilliSecond", (after - before));
					json.put("tabKey", tabKey);
					json.put("tabToken", tabToken);
					json.put("queryToken", queryToken);
					json.put("hasMore", hasMore);
					return BaseVO.success(json.toJSONString(), list);
				} else {
					return handleNonQuery(sqls, tabKey, tabToken);
				}
			} catch (Exception e) {
				if (e.getMessage().startsWith("400:")) {
					return handleNonQuery(sqls, tabKey, tabToken);
				}
				return new BaseVO<>(FeedbackError.QUERY_FAIL,
						new StringBuilder(MessageUtil.get(FeedbackError.QUERY_FAIL)).append(":").append(e.getMessage())
								.toString(),
						null);
			}
		} else {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, MessageUtil.get(FeedbackError.CHECK_FAIL), null);
		}
	}

	private BaseVO<Object> handleNonQuery(String sqls, String tabKey, String tabToken) {
		Long before = System.currentTimeMillis();
		try {
			iotDBController.getDetermineSessionPool().executeNonQueryStatement(sqls);
		} catch (Exception e) {
			return new BaseVO<>(FeedbackError.QUERY_FAIL, new StringBuilder(MessageUtil.get(FeedbackError.QUERY_FAIL))
					.append(":").append(e.getMessage()).toString(), null);
		}
		Long after = System.currentTimeMillis();

		JSONObject json = new JSONObject();
		json.put("costMilliSecond", (after - before));
		json.put("tabKey", tabKey);
		json.put("tabToken", tabToken);
		return BaseVO.success(json.toJSONString(), null);
	}

	@RequestMapping(value = "/api/query/updatePoint", method = { RequestMethod.POST })
	public BaseVO<Object> updatePointWithTenant(HttpServletRequest request,
			@RequestParam(value = "timestamp") Long timestamp, @RequestParam(value = "point") String point,
			@RequestParam(value = "value") String value) {
		if (point.indexOf('.') < 0) {
			return new BaseVO<>(FeedbackError.NO_SUPPORT_SQL, MessageUtil.get(FeedbackError.NO_SUPPORT_SQL), null);
		}
		String physical = point.substring(point.lastIndexOf('.') + 1, point.length());
		String entity = point.substring(0, point.lastIndexOf('.'));
		String sql = null;
		String ret = null;
		if ("".equals(value)) {
			StringBuilder sb = new StringBuilder("delete from ").append(entity).append(".").append(physical)
					.append(" where time = ").append(timestamp);
			sql = sb.toString();
			try {
				iotDBController.getDetermineSessionPool().executeNonQueryStatement(sql);
			} catch (Exception e) {
				return new BaseVO<>(FeedbackError.NO_SUPPORT_SQL,
						new StringBuilder(MessageUtil.get(FeedbackError.NO_SUPPORT_SQL)).append(": \"").append(sql)
								.append("\" :").append(e.getMessage()).toString(),
						null);
			}
		} else {
			StringBuilder sb = new StringBuilder("insert into ");
			boolean needQuotationMark = false;
			try {
				Long.parseLong(value);
			} catch (Exception e) {
				try {
					Double.parseDouble(value);
				} catch (Exception e2) {
					if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
						needQuotationMark = true;
					}
				}
			}
			sb.append(entity).append("(timestamp,").append(physical).append(") values(").append(timestamp).append(",");
			if (needQuotationMark) {
				sb.append("'").append(value).append("'");
			} else {
				sb.append(value);
			}
			sb.append(")");
			sql = sb.toString();
			try {
				iotDBController.getDetermineSessionPool().executeNonQueryStatement(sql);
			} catch (Exception e) {
				return new BaseVO<>(FeedbackError.NO_SUPPORT_SQL,
						new StringBuilder(MessageUtil.get(FeedbackError.NO_SUPPORT_SQL)).append(": \"").append(sql)
								.append("\" :").append(e.getMessage()).toString(),
						null);
			}
		}
		sql = String.format("select %s from %s where time=%d", physical, entity, timestamp);
		SessionDataSetWrapper sdsw = null;
		try {
			sdsw = iotDBController.getDetermineSessionPool().executeQueryStatement(sql);
			if (sdsw.hasNext()) {
				ret = sdsw.next().getFields().get(0).getStringValue();
			}
		} catch (IoTDBConnectionException | StatementExecutionException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (sdsw != null) {
				iotDBController.getDetermineSessionPool().closeResultSet(sdsw);
			}
		}
		return BaseVO.success("success", ret);
	}

	@RequestMapping(value = "/api/query/querySqlAppend", method = { RequestMethod.POST })
	public BaseVO<Object> querySqlAppendWithTenant(HttpServletRequest request,
			@RequestParam(value = "queryToken") String queryToken, @RequestParam(value = "tabKey") String tabKey,
			@RequestParam(value = "tabToken") String tabToken) {
		SessionDataSet ds = ContinuousIoTDBSession.getContinuousDataSet(queryToken);
		if (ds == null) {
			return new BaseVO<>(FeedbackError.NO_SESSION_DATASET, MessageUtil.get(FeedbackError.NO_SESSION_DATASET),
					null);
		}
		List<Map<String, Object>> list = new LinkedList<>();
		boolean hasMore = transformForQuery(list, ds, PAGE_SIZE);
		JSONObject json = new JSONObject();
		json.put("tabKey", tabKey);
		json.put("tabToken", tabToken);
		json.put("queryToken", queryToken);
		json.put("hasMore", hasMore);
		if (!hasMore) {
			ContinuousIoTDBSession.removeContinuousDataSet(queryToken);
		}
		return BaseVO.success(json.toJSONString(), list);
	}

	@RequestMapping(value = "/api/query/all", method = { RequestMethod.POST })
	public BaseVO<Object> queryAll(HttpServletRequest request, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "nameLike", required = false) String nameLike) {
		Long connectId = DynamicDataSourceAspect.getTenantCode(request);
		boolean b = false;
		try {
			b = checkConnectAuth(connectId);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
		if (b) {
			QueryCondition qc = new QueryCondition();
			qc.setConnectId(connectId);
			qc.setNameLike(nameLike);
			qc.setLimiter(new PageParam(pageNum, pageSize));
			qc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
			List<Query> list = queryDao.selectAll(qc);
			Page<Query> page = new Page<>(list, qc.getLimiter());
			return BaseVO.success(page);
		} else {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, MessageUtil.get(FeedbackError.CHECK_FAIL), null);
		}
	}

	@RequestMapping(value = "/api/query/deleteThenReturnAll", method = { RequestMethod.POST })
	public BaseVO<Object> deleteThenReturnAll(HttpServletRequest request, @RequestParam("queryId") Long queryId,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNum") Integer pageNum,
			@RequestParam(value = "nameLike", required = false) String nameLike) {
		Long connectId = DynamicDataSourceAspect.getTenantCode(request);
		boolean b = false;
		try {
			b = checkConnectAuth(connectId);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
		if (b) {
			Query q = new Query();
			q.setId(queryId);
			queryDao.delete(q);
			QueryCondition qc = new QueryCondition();
			qc.setConnectId(connectId);
			qc.setNameLike(nameLike);
			qc.setLimiter(new PageParam(pageNum, pageSize));
			qc.setSorter(new SortParam(new Order("id", Conditionable.Sequence.DESC)));
			List<Query> list = queryDao.selectAll(qc);
			Page<Query> page = new Page<>(list, qc.getLimiter());
			return BaseVO.success(page);
		} else {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, MessageUtil.get(FeedbackError.CHECK_FAIL), null);
		}
	}

	@RequestMapping(value = "/api/query/save", method = { RequestMethod.POST })
	public BaseVO<Object> querySave(HttpServletRequest request, @RequestParam("sqls") String sqls,
			@RequestParam("name") String name) {
		Long connectId = DynamicDataSourceAspect.getTenantCode(request);
		boolean b = false;
		try {
			b = checkConnectAuth(connectId);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
		if (b) {
			Query query = new Query();
			query.setConnectId(connectId);
			query.setName(name);
			query.setSqls(sqls);
			query.setCreateTime(Calendar.getInstance().getTime());
			try {
				transactionService.insertQueryTransactive(query, connectId);
			} catch (BaseException e) {
				return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
			}
			return BaseVO.success(null);
		} else {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, MessageUtil.get(FeedbackError.CHECK_FAIL), null);
		}
	}

	@RequestMapping(value = "/api/query/exportCsv", method = { RequestMethod.GET })
	public BaseVO<Object> queryExportCsvWithTenant(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "sqls") String sqls, @RequestParam(value = "timeformat") String timeformat,
			@RequestParam(value = "timeZone") String timeZone, @RequestParam(value = "targetFile") String targetFile,
			@RequestParam(value = "compress", required = false) String compress) throws IOException {
		Long connectId = DynamicDataSourceAspect.getTenantCode(request);
		String sessionId = IpUtils.getCookieValue(request, "JSESSIONID");
		boolean bb = false;
		try {
			bb = checkConnectAuth(connectId);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
		if (bb) {

			timeZone = timeZone.trim();
			if (!timeZone.startsWith("+") && !timeZone.startsWith("-")) {
				timeZone = new StringBuilder("+").append(timeZone).toString();
			}
			ExportTimeFormat exportTimeFormat = ExportTimeFormat.forValue(timeformat);
			CompressMode compressMode = CompressMode.forValue(compress);
			Connect connect = connectDao.select(connectId);
			Session session = new Session(connect.getHost(), connect.getPort(), connect.getUsername(),
					connect.getPassword());
			String respHeaderContent = new StringBuilder("attachment;filename=")
					.append(targetFile.isEmpty() ? "dump" : URLEncoder.encode(targetFile, "UTF-8"))
					.append(compressMode.getSuffix()).toString();
			response.setHeader("Content-Disposition", respHeaderContent);
			try (OutputStream stream = response.getOutputStream();) {
				session.open();
				SessionExportCsv.dumpResult(sqls, 0, session, stream, exportTimeFormat, timeZone, compressMode,
						sessionId);
			} catch (Exception e) {
			} finally {
				try {
					session.close();
				} catch (IoTDBConnectionException e) {
				}
			}
			return BaseVO.success(null);
		} else {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, MessageUtil.get(FeedbackError.CHECK_FAIL), null);
		}
	}

	@RequestMapping(value = "/api/query/importCsv", method = { RequestMethod.POST })
	public BaseVO<Object> queryImportCsvWithTenant(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "Filedata") MultipartFile file, @RequestParam(value = "timeZone") String timeZone,
			@RequestParam(value = "compress", required = false) String compress) throws IOException {
		Long connectId = DynamicDataSourceAspect.getTenantCode(request);
		String sessionId = IpUtils.getCookieValue(request, "JSESSIONID");
		boolean bb = false;
		try {
			bb = checkConnectAuth(connectId);
		} catch (BaseException e) {
			return new BaseVO<>(e.getErrorCode(), e.getMessage(), null);
		}
		if (bb) {
			timeZone = timeZone.trim();
			if (!timeZone.startsWith("+") && !timeZone.startsWith("-")) {
				timeZone = new StringBuilder("+").append(timeZone).toString();
			}
			CompressMode compressMode = CompressMode.forValue(compress);
			Connect connect = connectDao.select(connectId);
			if (!file.isEmpty()) {
				try {
					SessionImportCsv.importFromTargetPath(connect.getHost(), connect.getPort(), connect.getUsername(),
							connect.getPassword(), file.getResource().getInputStream(), timeZone, compressMode,
							sessionId);
				} catch (Exception e) {
					return new BaseVO<>(FeedbackError.IMPORT_CSV_FAIL,
							new StringBuilder(MessageUtil.get(FeedbackError.IMPORT_CSV_FAIL)).append(e.getMessage())
									.toString(),
							null);
				}
			}
			return BaseVO.success(null);
		} else {
			return new BaseVO<>(FeedbackError.CHECK_FAIL, MessageUtil.get(FeedbackError.CHECK_FAIL), null);
		}
	}
}
