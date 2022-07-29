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

	public static final String INSERT_CONN_FAIL = "CONN-10002";

	public static final String DELETE_CONN_FAIL = "CONN-10003";

	public static final String NO_CONN = "CONN-10004";

	public static final String CHECK_FAIL = "CONN-10006";

	public static final String TEST_CONN_FAIL = "CONN-10007";

	public static final String USER_AUTH_FAIL = "USER-10001";

	public static final String TEST_CONN_FAIL_PWD = "CONN-10010";

	public static final String UPDATE_CONN_SERVER_FAIL = "CONN-10020";

	public static final String INSERT_TS_FAIL = "IOTDB-10002";

	public static final String DELETE_TS_FAIL = "IOTDB-10003";

	public static final String QUERY_FAIL = "IOTDB-10009";

	public static final String WRONG_DB_PARAM = "IOTDB-10011";

	public static final String GET_USER_FAIL = "IOTDB-10012";

	public static final String GET_TIMESERIES_FAIL = "IOTDB-10013";

	public static final String SET_TTL_FAIL = "IOTDB-10014";

	public static final String SET_GROUP_FAIL = "IOTDB-10022";

	public static final String DELETE_GROUP_FAIL = "IOTDB-10023";

	public static final String SET_DB_USER_FAIL = "IOTDB-10026";

	public static final String PRIV_ROOT_FAIL = "IOTDB-10029";

	public static final String PRIV_GROUP_FAIL = "IOTDB-10030";

	public static final String PRIV_DEVICE_FAIL = "IOTDB-10031";

	public static final String PRIV_CHANGE_FAIL = "IOTDB-10032";

	public static final String UPDATE_PWD_FAIL = "IOTDB-10039";

	public static final String GET_STORAGE_FAIL = "IOTDB-10112";

	public static final String PRIV_TIMESERIES_FAIL = "IOTDB-10132";

	public static final String SELECT_CONNECTION_FAIL = "IOTDB-10001";

	public static final String NO_SESSION_DATASET = "IOTDB-10040";

	public static final String NO_SUPPORT_SQL = "IOTDB-10049";

	public static final String IMPORT_CSV_FAIL = "CSV-10008";

	public static final String DELETE_USER_FAIL = "USER-10006";

	public static final String DELETE_ACCOUNT_USER_FAIL = "USER-10007";

	public static final String CHANGE_ACCOUNT_USER_PASSWORD_FAIL = "USER-10008";

	public static final String QUERY_EXIST = "QUERY-10001";

	public static final String ACCOUNT_CAPTCHA_ERROR = "ACCOUNT-10001";

	public static final String ACCOUNT_REGISTER_ERROR = "ACCOUNT-10002";

	public static final String ACCOUNT_ACTIVATE_ERROR = "ACCOUNT-10003";

	public static final String ACCOUNT_EMAIL_ERROR = "ACCOUNT-10004";

	public static final String ACCOUNT_LOGIN_ERROR = "ACCOUNT-10005";

	public static final String ACCOUNT_FIND_USER_BY_EMAIL_ERROR = "ACCOUNT-10006";

	public static final String ACCOUNT_RESET_UPDATE_ERROR = "ACCOUNT-10007";

	public static final String ACCOUNT_RESET_EMAILLOG_ERROR = "ACCOUNT-10008";

	public static final String ACCOUNT_PASSWORD_ERROR = "ACCOUNT-10009";

	public static final String ACCOUNT_DELETE_ERROR = "ACCOUNT-10010";

	public static final String EXPORTER_NAME_REPEAT = "EXPORTER-10001";

	public static final String EXPORTER_ENDPOINT_REPEAT = "EXPORTER-10002";

	public static final String EXPORTER_CODE_REPEAT = "EXPORTER-10003";

	public static final String EXPORTER_DELETE_FAIL = "EXPORTER-10004";

	public static final String EXPORTER_GET_FAIL = "EXPORTER-10005";

	public static final String BOARD_TOKEN_REPEAT = "BOARD-10003";

	public static final String BOARD_DELETE_FAIL = "BOARD-10004";

	public static final String BOARD_GET_FAIL = "BOARD-10005";

	public static final String PANEL_REACH_LIMIT = "PANEL-10002";

	public static final String PANEL_NAME_REPEAT = "PANEL-10003";

	public static final String PANEL_DELETE_FAIL = "PANEL-10004";

	public static final String PANEL_GET_FAIL = "PANEL-10005";

	public static final String TASK_DELETE_FAIL = "TASK-10004";

	public static final String TASK_GET_FAIL = "TASK-10005";

	public static final String TASK_DELETE_FAIL_FOR_ALREADY_START = "TASK-10006";

	public static final String TASK_EDIT_FAIL_FOR_ALREADY_START = "TASK-10007";

	public static final String TASK_SHUTDOWN_FAIL = "TASK-10020";

	public static final String TASK_CHECK_PROCESS_FAIL = "TASK-10021";
}
