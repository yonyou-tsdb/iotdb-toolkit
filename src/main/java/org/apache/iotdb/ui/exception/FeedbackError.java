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
package org.apache.iotdb.ui.exception;

public class FeedbackError {

	public static final String ALIAS_REPEAT = "CONN-10001";
	public static final String ALIAS_REPEAT_MSG = "连接名称重复";

	public static final String INSERT_CONN_FAIL = "CONN-10002";
	public static final String INSERT_CONN_FAIL_MSG = "添加或更新连接失败";

	public static final String DELETE_CONN_FAIL = "CONN-10003";
	public static final String DELETE_CONN_FAIL_MSG = "删除连接失败";

	public static final String NO_CONN = "CONN-10004";
	public static final String NO_CONN_MSG = "连接不存在";

	public static final String CHECK_FAIL = "CONN-10006";
	public static final String CHECK_FAIL_MSG = "没有权限或连接不存在";

	public static final String TEST_CONN_FAIL = "CONN-10007";
	public static final String TEST_CONN_FAIL_MSG = "连接不可达或连接超时";

	public static final String USER_AUTH_FAIL = "USER-10008";
	public static final String USER_AUTH_FAIL_MSG = "用户不一致,不能进行操作";

	public static final String TEST_CONN_FAIL_PWD = "CONN-10010";
	public static final String TEST_CONN_FAIL_PWD_MSG = "连接失败，用户名或密码错误";

	public static final String INSERT_TS_FAIL = "IOTDB-10001";
	public static final String INSERT_TS_FAIL_MSG = "插入时间序列失败";

	public static final String DELETE_TS_FAIL = "IOTDB-10002";
	public static final String DELETE_TS_FAIL_MSG = "删除时间序列失败";

	public static final String QUERY_FAIL = "IOTDB-10009";
	public static final String QUERY_FAIL_MSG = "sql查询失败";

	public static final String WRONG_DB_PARAM = "IOTDB-10011";
	public static final String WRONG_DB_PARAM_MSG = "输入参数不合法";

	public static final String GET_USER_FAIL = "IOTDB-10012";
	public static final String GET_USER_FAIL_MSG = "获取用户信息失败";

	public static final String GET_TIMESERIES_FAIL = "IOTDB-10013";
	public static final String GET_TIMESERIES_FAIL_MSG = "获取时间序列信息失败";

	public static final String SET_TTL_FAIL = "IOTDB-10014";
	public static final String SET_TTL_FAIL_MSG = "设置存活时间失败";

	public static final String SET_GROUP_FAIL = "IOTDB-10022";
	public static final String SET_GROUP_FAIL_MSG = "创建存储组失败";

	public static final String DELETE_GROUP_FAIL = "IOTDB-10023";
	public static final String DELETE_GROUP_FAIL_MSG = "删除存储组失败";

	public static final String SET_DB_USER_FAIL = "IOTDB-10026";
	public static final String SET_DB_USER_FAIL_MSG = "创建数据库用户失败";

	public static final String PRIV_ROOT_FAIL = "IOTDB-10029";
	public static final String PRIV_ROOT_FAIL_MSG = "根路径权限操作失败";

	public static final String PRIV_GROUP_FAIL = "IOTDB-10030";
	public static final String PRIV_GROUP_FAIL_MSG = "组路径权限操作失败";

	public static final String PRIV_DEVICE_FAIL = "IOTDB-10031";
	public static final String PRIV_DEVICE_FAIL_MSG = "实体路径权限操作失败";

	public static final String PRIV_CHANGE_FAIL = "IOTDB-10032";
	public static final String PRIV_CHANGE_FAIL_MSG = "权限修改失败";

	public static final String PRIV_ADD_FAIL = "IOTDB-10033";
	public static final String PRIV_ADD_FAIL_MSG = "权限增加失败";

	public static final String UPDATE_PWD_FAIL = "IOTDB-10039";
	public static final String UPDATE_PWD_FAIL_MSG = "修改账号密码失败";

	public static final String GET_STORAGE_FAIL = "IOTDB-10112";
	public static final String GET_STORAGE_FAIL_MSG = "获取存储组信息失败";

	public static final String PRIV_TIMESERIES_FAIL = "IOTDB-10132";
	public static final String PRIV_TIMESERIES_FAIL_MSG = "物理量路径权限操作失败";

	public static final String SELECT_CONNECTION_FAIL = "IOTDB-10001";
	public static final String SELECT_CONNECTION_FAIL_MSG = "请选择数据源";

	public static final String NO_SESSION_DATASET = "IOTDB-10040";
	public static final String NO_SESSION_DATASET_MSG = "会话数据集已关闭";

	public static final String NO_SUPPORT_SQL = "IOTDB-10049";
	public static final String NO_SUPPORT_SQL_MSG = "不支持此sql执行";

	public static final String IMPORT_CSV_FAIL = "CSV-10008";
	public static final String IMPORT_CSV_FAIL_MSG = "导入csv文件失败，由于:";

	public static final String DELETE_USER_FAIL = "USER-10006";
	public static final String DELETE_USER_FAIL_MSG = "删除用户失败";

	public static final String QUERY_EXIST = "QUERY-10001";
	public static final String QUERY_EXIST_MSG = "脚本名已存在";

	public static final String ACCOUNT_CAPTCHA_ERROR = "ACCOUNT-10001";
	public static final String ACCOUNT_CAPTCHA_ERROR_MSG = "验证码有误";

	public static final String ACCOUNT_REGISTER_ERROR = "ACCOUNT-10002";
	public static final String ACCOUNT_REGISTER_ERROR_MSG = "注册的用户名或邮箱重复";
}
