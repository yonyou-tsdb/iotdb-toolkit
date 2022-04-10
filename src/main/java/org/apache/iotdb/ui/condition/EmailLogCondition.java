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
package org.apache.iotdb.ui.condition;

import java.util.Date;

import org.apache.iotdb.ui.entity.EmailLog;
import org.apache.iotdb.ui.entity.User;

import indi.mybatis.flying.annotations.ConditionMapperAnnotation;
import indi.mybatis.flying.annotations.Or;
import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.models.Limitable;
import indi.mybatis.flying.models.Sortable;
import indi.mybatis.flying.statics.ConditionType;

public class EmailLogCondition extends EmailLog implements Conditionable {

	private Limitable limiter;

	private Sortable sorter;

	public Limitable getLimiter() {
		return limiter;
	}

	public void setLimiter(Limitable limiter) {
		this.limiter = limiter;
	}

	public Sortable getSorter() {
		return sorter;
	}

	public void setSorter(Sortable sorter) {
		this.sorter = sorter;
	}

	@Or({ @ConditionMapperAnnotation(dbFieldName = "email", conditionType = ConditionType.EQUAL),
			@ConditionMapperAnnotation(dbFieldName = "name", conditionType = ConditionType.EQUAL, subTarget = User.class), })
	private Object[] emailEqualOrUsernameEqual;

	@ConditionMapperAnnotation(dbFieldName = "email_time", conditionType = ConditionType.GREATER_THAN)
	private Date emailTimeGreaterThan;

	@ConditionMapperAnnotation(dbFieldName = "email_time", conditionType = ConditionType.LESS_OR_EQUAL)
	private Date emailTimeLessOrEqual;

	@Or({ @ConditionMapperAnnotation(dbFieldName = "account_id", conditionType = ConditionType.EQUAL),
			@ConditionMapperAnnotation(dbFieldName = "temp_account", conditionType = ConditionType.EQUAL), })
	private Object[] accountIdEqualOrTempAccountEqual;

	@ConditionMapperAnnotation(dbFieldName = "account_id", conditionType = ConditionType.EQUAL)
	private Long accountIdEqual;

	@ConditionMapperAnnotation(dbFieldName = "temp_account", conditionType = ConditionType.EQUAL)
	private String tempAccountEqual;

	public Object[] getEmailEqualOrUsernameEqual() {
		return emailEqualOrUsernameEqual;
	}

	public void setEmailEqualOrUsernameEqual(Object... emailEqualOrUsernameEqual) {
		this.emailEqualOrUsernameEqual = emailEqualOrUsernameEqual;
	}

	public Date getEmailTimeGreaterThan() {
		return emailTimeGreaterThan;
	}

	public void setEmailTimeGreaterThan(Date emailTimeGreaterThan) {
		this.emailTimeGreaterThan = emailTimeGreaterThan;
	}

	public Date getEmailTimeLessOrEqual() {
		return emailTimeLessOrEqual;
	}

	public void setEmailTimeLessOrEqual(Date emailTimeLessOrEqual) {
		this.emailTimeLessOrEqual = emailTimeLessOrEqual;
	}

	public Object[] getAccountIdEqualOrTempAccountEqual() {
		return accountIdEqualOrTempAccountEqual;
	}

	public void setAccountIdEqualOrTempAccountEqual(Object... accountIdEqualOrTempAccountEqual) {
		this.accountIdEqualOrTempAccountEqual = accountIdEqualOrTempAccountEqual;
	}

	public Long getAccountIdEqual() {
		return accountIdEqual;
	}

	public void setAccountIdEqual(Long accountIdEqual) {
		this.accountIdEqual = accountIdEqual;
	}

	public String getTempAccountEqual() {
		return tempAccountEqual;
	}

	public void setTempAccountEqual(String tempAccountEqual) {
		this.tempAccountEqual = tempAccountEqual;
	}

}
