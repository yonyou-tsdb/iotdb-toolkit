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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.ui.config.tsdatasource.DynamicSessionPool;
import org.apache.iotdb.ui.exception.ErrorCode;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.model.Granularity;
import org.apache.iotdb.ui.model.UserDto;
import org.apache.iotdb.ui.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@Api(value = "IoTDB API")
public class IotDBController {

	public static final String REG = "^((?!;).)*$";

	public static final String REG2 = "^((?!').)*$";

	@Autowired
	private QueryController queryController;

	@Autowired
	@Qualifier("dynamicSessionPool")
	private DynamicSessionPool dynamicSessionPool;

	public SessionPool getDetermineSessionPool() {
		return dynamicSessionPool.determineTargetSessionPool();
	}

	@ApiOperation(value = "/api/iotdb/listUser", notes = "/api/iotdb/listUser")
	@RequestMapping(value = "/api/iotdb/listUser", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> listUserWithTenant(HttpServletRequest request) throws SQLException {
		String sql = "list user";
		try {
			List<Map<String, Object>> list = queryController
					.transform(getDetermineSessionPool().executeQueryStatement(sql));
			return BaseVO.success(list);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.GET_USER_FAIL,
					new StringBuilder(ErrorCode.GET_USER_FAIL_MSG).append(":").append(e.getMessage()).toString(), null);
		}
	}

	class CompareByLength implements Comparator<Map<String, Object>> {

		public CompareByLength(String... compareKey) {
			this.compareKeys = compareKey;
		}

		private String[] compareKeys;

		@Override
		public int compare(Map<String, Object> s1, Map<String, Object> s2) { // 按照字符串的长度比较

			if (compareKeys == null) {
				return 0;
			} else {
				for (String s : compareKeys) {
					int num = 0;
					if (s1.get(s) instanceof String && s2.get(s) instanceof String) {
						num = s1.get(s).toString().compareTo(s2.get(s).toString());
					} else {
						num = Integer.parseInt(s1.get(s).toString()) - Integer.parseInt(s2.get(s).toString());
					}
					if (num != 0) {
						return num;
					}
				}
			}

			return 0;
		}
	}

	@ApiOperation(value = "/api/iotdb/listPrivileges", notes = "/api/iotdb/listPrivileges")
	@RequestMapping(value = "/api/iotdb/listPrivileges", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> listPrivilegesWithTenant(HttpServletRequest request, @RequestParam("user") String user)
			throws SQLException {
		String sql = new StringBuilder("list user privileges ").append(user).toString();
		if (!sql.matches(REG)) {
			return new BaseVO<>(ErrorCode.WRONG_DB_PARAM, ErrorCode.WRONG_DB_PARAM_MSG, null);
		}
		try {
			List<Map<String, Object>> list = queryController
					.transform(getDetermineSessionPool().executeQueryStatement(sql));
			List<HashMap<String, Object>> list1 = new ArrayList<>();
			int i = 1;
			for (Map<String, Object> e : list) {
				Object o = e.get("privilege");
				if (o == null) {
					continue;
				}
				String s = o.toString();
				String[] array = s.split(":");
				// deal granularity
				int c = CommonUtils.countOccurrences(array[0], ".");
				Granularity granularity = checkGranularity(c);
				HashMap<String, Object> m = new HashMap<>();
				m.put("index", granularity.getIndex());
				m.put("granularity", granularity.getValue());
				m.put("depth", c);
				m.put("range", array[0].trim());
				m.put("auth", array[1].trim().split(" "));
				m.put("key", i++);
				list1.add(m);
			}
			Collections.sort(list1, new CompareByLength("index", "range"));
			return BaseVO.success(list1);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.GET_USER_FAIL,
					new StringBuilder(ErrorCode.GET_USER_FAIL_MSG).append(":").append(e.getMessage()).toString(), null);
		}
	}

	private Granularity checkGranularity(int c) {
		Granularity granularity = null;
		switch (c) {
		case 0:
			granularity = Granularity.CONNECTION;
			break;
		case 1:
			granularity = Granularity.STORAGE_GROUP;
			break;
		case 2:
			granularity = Granularity.ENTITY;
			break;
		default:
			granularity = Granularity.PHYSICAL;
		}
		return granularity;
	}

	@ApiOperation(value = "/api/iotdb/changePrivileges", notes = "/api/iotdb/changePrivileges")
	@RequestMapping(value = "/api/iotdb/changePrivileges", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> changePrivilegesWithTenant(HttpServletRequest request, @RequestParam("user") String user,
			@RequestParam(value = "auth", required = false) String auths, @RequestParam(value = "range") String ranges)
			throws Exception {
		if (!(user.matches(REG) && (auths == null || auths.matches(REG)) && ranges.matches(REG))) {
			return new BaseVO<>(ErrorCode.WRONG_DB_PARAM, ErrorCode.WRONG_DB_PARAM_MSG, null);
		}
		List<String> sqlList = new ArrayList<>();
		String[] rangeArray = ranges.split(",");
		Set<String> rangeSet = new HashSet<>();
		for (String s : rangeArray) {
			rangeSet.add(s.trim());
		}
		Set<String> authSet = new HashSet<>();
		if (auths != null) {
			String[] authArray = auths.split(",");
			for (String s : authArray) {
				authSet.add(s.trim());
			}
		}
		Map<String, Set<String>> m0 = new HashMap<>();
		for (String r : rangeSet) {
			String sql = new StringBuilder("list privileges user ").append(user).append(" on ").append(r).toString();
			List<Map<String, Object>> list = queryController
					.transform(getDetermineSessionPool().executeQueryStatement(sql));
			for (Map<String, Object> e : list) {
				String t = e.get("privilege").toString().split(":")[0].trim();
				if (rangeSet.contains(t)) {
					if (m0.get(t) == null) {
						m0.put(t, new HashSet<>());
					}
					m0.get(t).addAll(Arrays.stream(e.get("privilege").toString().split(":")[1].trim().split(" "))
							.collect(Collectors.toSet()));
				}
			}
		}
		Map<String, Set<String>> unchange = new HashMap<>();
		for (Map.Entry<String, Set<String>> e : m0.entrySet()) {
			for (String ss : e.getValue()) {
				if (rangeSet.contains(e.getKey()) && authSet.contains(ss)) {
					if (unchange.get(e.getKey()) == null) {
						unchange.put(e.getKey(), new HashSet<>());
					}
					unchange.get(e.getKey()).add(ss);
				} else {
					sqlList.add(new StringBuilder("REVOKE USER ").append(user).append(" PRIVILEGES '").append(ss)
							.append("' on ").append(e.getKey()).append(";").toString());
				}
			}
		}

		for (String r : rangeSet) {
			for (String a : authSet) {
				if (unchange.get(r) == null || !unchange.get(r).contains(a)) {
					sqlList.add(new StringBuilder("GRANT USER ").append(user).append(" PRIVILEGES '").append(a)
							.append("' on ").append(r).append(";").toString());
				}
			}
		}
		try {
			for (String s : sqlList) {
				getDetermineSessionPool().executeNonQueryStatement(s);
			}
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.PRIV_CHANGE_FAIL,
					new StringBuilder(ErrorCode.PRIV_CHANGE_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "/api/iotdb/showSchema", notes = "/api/iotdb/showSchema")
	@RequestMapping(value = "/api/iotdb/showSchema", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> showSchemaWithTenant(HttpServletRequest request) throws Exception {
		JSONArray ret = new JSONArray();
		TreeSet<String> r1 = new TreeSet<String>();
		List<Map<String, Object>> list1 = queryController
				.transform(getDetermineSessionPool().executeQueryStatement("show storage group"));
		if (list1 != null) {
			for (Map<String, Object> m : list1) {
				if (m != null) {
					Object o = m.get("storage group");
					if (o != null) {
						r1.add(o.toString());
					}
				}
			}
		}
		List<Map<String, Object>> list2 = queryController
				.transform(getDetermineSessionPool().executeQueryStatement("show timeseries"));

		TreeMap<String, Object> r2 = new TreeMap<String, Object>();
		TreeMap<String, Object> r3 = new TreeMap<String, Object>();
		if (list2 != null) {
			for (Map<String, Object> m : list2) {
				Object os = m.get("storage group");
				Object ot = m.get("timeseries");
				if (os != null && ot != null) {
					String sg = os.toString();
					String ts = ot.toString();
					if (ts.startsWith(new StringBuilder(sg).append(".").toString())) {
						Set<String> r11 = null;
						if (!r2.containsKey(sg)) {
							r11 = new HashSet<>();
							r2.put(sg, r11);
						} else {
							r11 = (Set<String>) r2.get(sg);
						}
						String physical = null;
						ts = ts.substring(sg.length() + 1, ts.length());
						if (ts.indexOf('.') > -1) {
							physical = ts.substring(ts.lastIndexOf('.') + 1, ts.length());
							ts = ts.substring(0, ts.lastIndexOf('.'));
						}
						String entity = ts;
						r11.add(entity);
						Set<String> r21 = null;
						if (!r3.containsKey(entity)) {
							r21 = new HashSet<>();
							r3.put(entity, r21);
						} else {
							r21 = (Set<String>) r3.get(entity);
						}
						if (physical != null) {
							r21.add(physical);
						}
					}
				}
			}
		}
		ret.add(r1);
		ret.add(r2);
		ret.add(r3);
		return BaseVO.success(ret);
	}

	@ApiOperation(value = "/api/iotdb/addPrivileges", notes = "/api/iotdb/addPrivileges")
	@RequestMapping(value = "/api/iotdb/addPrivileges", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> addPrivilegesWithTenant(HttpServletRequest request, @RequestParam("user") String user,
			@RequestParam(value = "auth") String authStr, @RequestParam(value = "sg", required = false) String sgStr,
			@RequestParam(value = "entity", required = false) String entityStr,
			@RequestParam(value = "physical", required = false) String physicalStr) throws SQLException {
		if (!(user.matches(REG) && authStr.matches(REG) && (sgStr == null || sgStr.matches(REG))
				&& (entityStr == null || entityStr.matches(REG))
				&& (physicalStr == null || physicalStr.matches(REG)))) {
			return new BaseVO<>(ErrorCode.WRONG_DB_PARAM, ErrorCode.WRONG_DB_PARAM_MSG, null);
		}
		List<String> sqlList = new ArrayList<>();
		String[] auths = authStr.trim().split(",");
		String[] sgs = sgStr == null ? new String[] {} : sgStr.trim().split(",");
		String[] entities = entityStr == null ? new String[] {} : entityStr.trim().split(",");
		String[] physicals = physicalStr == null ? new String[] {} : physicalStr.trim().split(",");
		for (String a : auths) {
			if (sgs.length == 0) {
				sqlList.add(new StringBuilder("grant user ").append(user).append(" privileges '").append(a)
						.append("' on root").toString());
			} else {
				for (String s : sgs) {
					if (entities.length == 0) {
						sqlList.add(new StringBuilder("grant user ").append(user).append(" privileges '").append(a)
								.append("' on ").append(s).toString());
					} else {
						for (String e : entities) {
							if (physicals.length == 0) {
								sqlList.add(new StringBuilder("grant user ").append(user).append(" privileges '")
										.append(a).append("' on ").append(s).append(".").append(e).toString());
							} else {
								for (String p : physicals) {
									sqlList.add(new StringBuilder("grant user ").append(user).append(" privileges '")
											.append(a).append("' on ").append(s).append(".").append(e).append(".")
											.append(p).toString());
								}
							}
						}
					}
				}
			}
		}
		try {
			for (String s : sqlList) {
				getDetermineSessionPool().executeNonQueryStatement(s);
			}
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.PRIV_ADD_FAIL,
					new StringBuilder(ErrorCode.PRIV_ADD_FAIL_MSG).append(":").append(e.getMessage()).toString(), null);
		}
		return BaseVO.success(null);
	}

	@ApiOperation(value = "/api/iotdb/addUser/{user}", notes = "/api/iotdb/addUser/{user}")
	@RequestMapping(value = "/api/iotdb/addUser/{user}", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> addUserWithTenant(HttpServletRequest request, @Valid UserDto userDto) throws SQLException {
		try {
			getDetermineSessionPool().executeNonQueryStatement(new StringBuilder("CREATE USER ")
					.append(userDto.getUser()).append(" '").append(userDto.getPassword()).append("'").toString());
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.SET_DB_USER_FAIL,
					new StringBuilder(ErrorCode.SET_DB_USER_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@ApiOperation(value = "/api/iotdb/editUser", notes = "/api/iotdb/editUser")
	@RequestMapping(value = "/api/iotdb/editUser", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> editUserWithTenant(HttpServletRequest request, @RequestParam("user") String user,
			@RequestParam(value = "password") String password) throws SQLException {
		if (!(user.matches(REG) && password.matches(REG))) {
			return new BaseVO<>(ErrorCode.WRONG_DB_PARAM, ErrorCode.WRONG_DB_PARAM_MSG, null);
		}
		try {
			getDetermineSessionPool().executeNonQueryStatement(new StringBuilder("alter user ").append(user)
					.append(" set password '").append(password).append("'").toString());
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.UPDATE_PWD_FAIL,
					new StringBuilder(ErrorCode.UPDATE_PWD_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/iotdb/deleteUser", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> deleteUserWithTenant(HttpServletRequest request, @RequestParam("user") String user)
			throws SQLException {
		if (!(user.matches(REG))) {
			return new BaseVO<>(ErrorCode.WRONG_DB_PARAM, ErrorCode.WRONG_DB_PARAM_MSG, null);
		}
		try {
			getDetermineSessionPool().executeNonQueryStatement(new StringBuilder("drop user ").append(user).toString());
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.DELETE_USER_FAIL,
					new StringBuilder(ErrorCode.DELETE_USER_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/iotdb/showStorage", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> showStorageWithTenant(HttpServletRequest request) throws SQLException {
		try {
			List<Map<String, Object>> list = queryController
					.transform(getDetermineSessionPool().executeQueryStatement("show storage group"));
			List<Map<String, Object>> list2 = queryController
					.transform(getDetermineSessionPool().executeQueryStatement("show timeseries"));
			List<Map<String, Object>> list3 = queryController
					.transform(getDetermineSessionPool().executeQueryStatement("show all ttl"));
			Map<String, Set<String>> m2 = new HashMap<>();
			for (Map<String, Object> e : list2) {
				if (e == null || e.get("timeseries") == null || e.get("storage group") == null) {
					continue;
				}
				String timeseries = e.get("timeseries").toString();
				String sg = e.get("storage group").toString();
				if (!m2.containsKey(sg)) {
					m2.put(sg, new HashSet<String>());
				}
				m2.get(sg).add(timeseries);
			}
			Map<String, Long> m3 = new HashMap<>();
			for (Map<String, Object> e : list3) {
				if (e == null || e.get("ttl") == null || e.get("storage group") == null) {
					continue;
				}
				Long ttl = Long.valueOf(e.get("ttl").toString());
				String sg = e.get("storage group").toString();
				m3.put(sg, ttl);
			}

			Collections.sort(list, new CompareByLength("storage group"));
			for (Map<String, Object> m : list) {
				if (m != null) {
					Object sgo = m.get("storage group");
					Set<String> s = m2.get(sgo);
					m.put("timeseriesCount", s == null ? 0 : s.size());
					m.put("ttl", m3.get(sgo));
					m.put("value", sgo);
					m.remove("storage group");
				}
			}
			return BaseVO.success(list);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.GET_STORAGE_FAIL,
					new StringBuilder(ErrorCode.GET_STORAGE_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
	}

	@RequestMapping(value = "/api/iotdb/addStorageGroup", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> addStorageGroupWithTenant(HttpServletRequest request, @RequestParam("name") String name,
			@RequestParam(value = "ttl", required = false) Long ttl) throws SQLException {
		try {
			getDetermineSessionPool().setStorageGroup(name);
		} catch (Exception e2) {
			return new BaseVO<>(ErrorCode.SET_GROUP_FAIL,
					new StringBuilder(ErrorCode.SET_GROUP_FAIL_MSG).append(":").append(e2.getMessage()).toString(),
					null);
		}
		if (ttl != null) {
			try {
				getDetermineSessionPool().executeNonQueryStatement(
						new StringBuilder("set ttl to ").append(name).append(" ").append(ttl).toString());
			} catch (Exception e) {
				return new BaseVO<>(ErrorCode.SET_TTL_FAIL,
						new StringBuilder(ErrorCode.SET_TTL_FAIL_MSG).append(":").append(e.getMessage()).toString(),
						null);
			}
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/iotdb/deleteStorageGroup", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> deleteStorageGroupWithTenant(HttpServletRequest request, @RequestParam("name") String name)
			throws SQLException {
		String sql = new StringBuilder("delete storage group ").append(name).toString();
		try {
			getDetermineSessionPool().executeNonQueryStatement(sql);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.DELETE_GROUP_FAIL,
					new StringBuilder(ErrorCode.DELETE_GROUP_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/iotdb/editStorageGroup", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> editStorageGroupWithTenant(HttpServletRequest request, @RequestParam("name") String name,
			@RequestParam(value = "ttl", required = false) Long ttl) throws SQLException {
		String sql = null;
		if (ttl == null) {
			sql = new StringBuilder("unset ttl to ").append(name).toString();
		} else {
			sql = new StringBuilder("set ttl to ").append(name).append(" ").append(ttl).toString();
		}
		try {
			getDetermineSessionPool().executeNonQueryStatement(sql);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.SET_GROUP_FAIL,
					new StringBuilder(ErrorCode.SET_GROUP_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/iotdb/showTimeseries", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> showTimeseriesWithTenant(HttpServletRequest request, @RequestParam("path") String path)
			throws SQLException {
		String sql0 = new StringBuilder("show timeseries ").append(path).toString();
		try {
			List<Map<String, Object>> list0 = queryController
					.transform(getDetermineSessionPool().executeQueryStatement(sql0));
			int j = 1;
			for (Map<String, Object> e : list0) {
				e.put("key", j++);
				String[] array = { e.get("dataType").toString(), e.get("encoding").toString(),
						e.get("compression").toString() };
				e.put("auth", array);
				String sg = e.get("storage group").toString();
				String timeseries = e.get("timeseries").toString();
				String range = (timeseries.startsWith(sg) && timeseries.lastIndexOf('.') - sg.length() > 1)
						? timeseries.substring(sg.length() + 1, timeseries.lastIndexOf('.'))
						: "";
				e.put("granularity", range);
				timeseries = timeseries.substring(timeseries.lastIndexOf('.') + 1, timeseries.length());
				e.put("range", timeseries);
			}
			Collections.sort(list0, new CompareByLength("granularity", "range"));
			return BaseVO.success(list0);
		} catch (Exception e) {
			e.printStackTrace();
			return new BaseVO<>(ErrorCode.GET_TIMESERIES_FAIL,
					new StringBuilder(ErrorCode.GET_TIMESERIES_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
	}

	@RequestMapping(value = "/api/iotdb/deleteTimeseries", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> deleteTimeseriesWithTenant(HttpServletRequest request, @RequestParam("path") String path)
			throws SQLException {
		String sql = new StringBuilder("delete timeseries ").append(path).toString();
		try {
			getDetermineSessionPool().executeNonQueryStatement(sql);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.DELETE_TS_FAIL,
					new StringBuilder(ErrorCode.DELETE_TS_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

	@RequestMapping(value = "/api/iotdb/addTimeseries", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> addTimeseriesWithTenant(HttpServletRequest request, @RequestParam("path") String path,
			@RequestParam(value = "dataType") String dataType, @RequestParam(value = "encoding") String encoding)
			throws SQLException {
		String sql = new StringBuilder("create timeseries ").append(path).append(" with datatype =").append(dataType)
				.append(",encoding=").append(encoding).toString();
		try {
			getDetermineSessionPool().executeNonQueryStatement(sql);
		} catch (Exception e) {
			return new BaseVO<>(ErrorCode.INSERT_TS_FAIL,
					new StringBuilder(ErrorCode.INSERT_TS_FAIL_MSG).append(":").append(e.getMessage()).toString(),
					null);
		}
		return BaseVO.success(null);
	}

}
