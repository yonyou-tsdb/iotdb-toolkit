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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.ConnectFace;
import org.apache.iotdb.ui.face.EmailLogFace;
import org.apache.iotdb.ui.face.UserFace;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_user")
public class User extends PojoSupport implements UserFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 用户名
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "name", jdbcType = JdbcType.VARCHAR)
	private String name;

	/**
	 * 密码
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "password", jdbcType = JdbcType.VARCHAR)
	private String password;

	/**
	 * 设置
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "setting", jdbcType = JdbcType.VARCHAR)
	private JSONObject setting;

	@JSONField(serialize = false)
	private Map<Object, ConnectFace> connectMap;

	@JSONField(serialize = false)
	private Map<Object, EmailLogFace> emailLogMap;

	public Map<Object, ? extends ConnectFace> getConnectMap() {
		if (connectMap == null) {
			connectMap = new LinkedHashMap<Object, ConnectFace>();
		}
		return connectMap;
	}

	public Map<Object, ? extends EmailLogFace> getEmailLogMap() {
		if (emailLogMap == null) {
			emailLogMap = new LinkedHashMap<Object, EmailLogFace>();
		}
		return emailLogMap;
	}

	public Collection<? extends ConnectFace> getConnect() {
		return getConnectMap().values();
	}

	private Iterator<? extends ConnectFace> getIteratorConnect() {
		return getConnect().iterator();
	}

	public void setConnect(Collection<? extends ConnectFace> newConnect) {
		removeAllConnect();
		for (Iterator<? extends ConnectFace> iter = newConnect.iterator(); iter.hasNext();)
			addConnect(iter.next());
	}

	public void addConnect(ConnectFace newConnect) {
		if (newConnect == null)
			return;
		if (this.connectMap == null)
			this.connectMap = new LinkedHashMap<Object, ConnectFace>();
		if (!this.connectMap.containsKey(newConnect.getId())) {
			this.connectMap.put(newConnect.getId(), newConnect);
			newConnect.setUser(this);
		} else {
			ConnectFace temp = connectMap.get(newConnect.getId());
			if (newConnect.equals(temp) && temp != newConnect) {
				removeConnect(temp);
				this.connectMap.put(newConnect.getId(), newConnect);
				newConnect.setUser(this);
			}
		}
	}

	public void removeConnect(ConnectFace oldConnect) {
		if (oldConnect == null)
			return;
		if (this.connectMap != null && this.connectMap.containsKey(oldConnect.getId())) {
			ConnectFace temp = connectMap.get(oldConnect.getId());
			if (oldConnect.equals(temp) && temp != oldConnect) {
				temp.setUser(null);
			}
			this.connectMap.remove(oldConnect.getId());
			oldConnect.setUser(null);
		}
	}

	public void removeAllConnect() {
		if (connectMap != null) {
			ConnectFace oldConnect;
			for (Iterator<? extends ConnectFace> iter = getIteratorConnect(); iter.hasNext();) {
				oldConnect = iter.next();
				iter.remove();
				oldConnect.setUser(null);
			}
		}
	}

	public Collection<? extends EmailLogFace> getEmailLog() {
		return getEmailLogMap().values();
	}

	private Iterator<? extends EmailLogFace> getIteratorEmailLog() {
		return getEmailLog().iterator();
	}

	public void setEmailLog(Collection<? extends EmailLogFace> newEmailLog) {
		removeAllEmailLog();
		for (Iterator<? extends EmailLogFace> iter = newEmailLog.iterator(); iter.hasNext();)
			addEmailLog(iter.next());
	}

	public void addEmailLog(EmailLogFace newEmailLog) {
		if (newEmailLog == null)
			return;
		if (this.emailLogMap == null)
			this.emailLogMap = new LinkedHashMap<Object, EmailLogFace>();
		if (!this.emailLogMap.containsKey(newEmailLog.getId())) {
			this.emailLogMap.put(newEmailLog.getId(), newEmailLog);
			newEmailLog.setUser(this);
		} else {
			EmailLogFace temp = emailLogMap.get(newEmailLog.getId());
			if (newEmailLog.equals(temp) && temp != newEmailLog) {
				removeEmailLog(temp);
				this.emailLogMap.put(newEmailLog.getId(), newEmailLog);
				newEmailLog.setUser(this);
			}
		}
	}

	public void removeEmailLog(EmailLogFace oldEmailLog) {
		if (oldEmailLog == null)
			return;
		if (this.emailLogMap != null && this.emailLogMap.containsKey(oldEmailLog.getId())) {
			EmailLogFace temp = emailLogMap.get(oldEmailLog.getId());
			if (oldEmailLog.equals(temp) && temp != oldEmailLog) {
				temp.setUser(null);
			}
			this.emailLogMap.remove(oldEmailLog.getId());
			oldEmailLog.setUser(null);
		}
	}

	public void removeAllEmailLog() {
		if (emailLogMap != null) {
			EmailLogFace oldEmailLog;
			for (Iterator<? extends EmailLogFace> iter = getIteratorEmailLog(); iter.hasNext();) {
				oldEmailLog = iter.next();
				iter.remove();
				oldEmailLog.setUser(null);
			}
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public JSONObject getSetting() {
		return setting;
	}

	public void setSetting(JSONObject setting) {
		this.setting = setting;
	}

}
