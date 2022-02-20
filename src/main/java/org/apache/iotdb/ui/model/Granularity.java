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
package org.apache.iotdb.ui.model;

import org.apache.iotdb.ui.exception.FeedbackError;

public enum Granularity {

	CONNECTION("数据连接", 1, FeedbackError.PRIV_ROOT_FAIL, FeedbackError.PRIV_ROOT_FAIL_MSG),
	STORAGE_GROUP("存储组", 2, FeedbackError.PRIV_GROUP_FAIL, FeedbackError.PRIV_GROUP_FAIL_MSG),
	ENTITY("实体", 3, FeedbackError.PRIV_DEVICE_FAIL, FeedbackError.PRIV_DEVICE_FAIL_MSG),
	PHYSICAL("物理量", 4, FeedbackError.PRIV_TIMESERIES_FAIL, FeedbackError.PRIV_TIMESERIES_FAIL_MSG),
	UNKNOWN("未知", 5, "UNKNOWN", "UNKNOWN");

	private final String value;

	private final int index;

	private final String errorCode;

	private final String errorMsg;

	private Granularity(String value, int index, String errorCode, String errorMsg) {
		this.value = value;
		this.index = index;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public String getValue() {
		return value;
	}

	public int getIndex() {
		return index;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

}
