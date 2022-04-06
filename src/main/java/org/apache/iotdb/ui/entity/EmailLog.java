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
package org.apache.iotdb.ui.entity;

import java.util.Date;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.EmailLogFace;
import org.apache.iotdb.ui.face.UserFace;
import org.apache.iotdb.ui.handler.EmailLogStatusHandler;
import org.apache.iotdb.ui.model.EmailLogStatus;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_email_log")
public class EmailLog extends PojoSupport implements EmailLogFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 邮箱
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "email", jdbcType = JdbcType.VARCHAR)
	private String email;

	/**
	 * 重置请求时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "email_time", jdbcType = JdbcType.TIMESTAMP)
	private Date emailTime;

	/**
	 * 链接有效期到期时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "due_time", jdbcType = JdbcType.TIMESTAMP)
	private Date dueTime;

	/**
	 * 重置发生时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "reset_time", jdbcType = JdbcType.TIMESTAMP)
	private Date resetTime;

	/**
	 * 此次重置密码的随机数token
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "token", jdbcType = JdbcType.VARCHAR)
	private String token;

	/**
	 * 链接是否可用。若用户使用此链接修改密码成功后便不再可用。
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "available", jdbcType = JdbcType.BOOLEAN)
	private Boolean available;

	/**
	 * 当为i时表示新增用户，当为u时表示更新用户，当为t时表示租户邀请；
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "status", jdbcType = JdbcType.CHAR, customTypeHandler = EmailLogStatusHandler.class)
	private EmailLogStatus status;

	/**
	 * 用户账号，当此条记录为激活账号记录时才有需要
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "temp_account", jdbcType = JdbcType.VARCHAR)
	private String tempAccount;

	/**
	 * 用户密码，当此条记录为激活账号记录时才有需要
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "temp_password", jdbcType = JdbcType.VARCHAR)
	private String tempPassword;

	@FieldMapperAnnotation(dbFieldName = "account_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "account_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeEmailLog(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addEmailLog(this);
			}
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getEmailTime() {
		return emailTime;
	}

	public void setEmailTime(Date emailTime) {
		this.emailTime = emailTime;
	}

	public Date getDueTime() {
		return dueTime;
	}

	public void setDueTime(Date dueTime) {
		this.dueTime = dueTime;
	}

	public Date getResetTime() {
		return resetTime;
	}

	public void setResetTime(Date resetTime) {
		this.resetTime = resetTime;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public EmailLogStatus getStatus() {
		return status;
	}

	public void setStatus(EmailLogStatus status) {
		this.status = status;
	}

	public String getTempAccount() {
		return tempAccount;
	}

	public void setTempAccount(String tempAccount) {
		this.tempAccount = tempAccount;
	}

	public String getTempPassword() {
		return tempPassword;
	}

	public void setTempPassword(String tempPassword) {
		this.tempPassword = tempPassword;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
