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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.ConnectFace;
import org.apache.iotdb.ui.face.QueryFace;
import org.apache.iotdb.ui.face.UserFace;

import com.alibaba.fastjson.annotation.JSONField;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_connect")
public class Connect extends PojoSupport implements ConnectFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 地址
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "host", jdbcType = JdbcType.VARCHAR)
	private String host;

	/**
	 * 端口
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "port", jdbcType = JdbcType.INTEGER)
	private Integer port;

	/**
	 * 数据源用户名
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "username", jdbcType = JdbcType.VARCHAR)
	private String username;

	/**
	 * 数据源密码
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "password", jdbcType = JdbcType.VARCHAR)
	private String password;

	/**
	 * 别名
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "alias", jdbcType = JdbcType.VARCHAR)
	private String alias;

	@FieldMapperAnnotation(dbFieldName = "create_time", jdbcType = JdbcType.TIMESTAMP)
	private Date createTime;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	@JSONField(serialize = false)
	private Map<Object, QueryFace> queryMap;

	public Map<Object, ? extends QueryFace> getQueryMap() {
		if (queryMap == null) {
			queryMap = new LinkedHashMap<Object, QueryFace>();
		}
		return queryMap;
	}

	public Collection<? extends QueryFace> getQuery() {
		return getQueryMap().values();
	}

	private Iterator<? extends QueryFace> getIteratorQuery() {
		return getQuery().iterator();
	}

	public void setQuery(Collection<? extends QueryFace> newQuery) {
		removeAllQuery();
		for (Iterator<? extends QueryFace> iter = newQuery.iterator(); iter.hasNext();)
			addQuery(iter.next());
	}

	public void addQuery(QueryFace newQuery) {
		if (newQuery == null)
			return;
		if (this.queryMap == null)
			this.queryMap = new LinkedHashMap<Object, QueryFace>();
		if (!this.queryMap.containsKey(newQuery.getId())) {
			this.queryMap.put(newQuery.getId(), newQuery);
			newQuery.setConnect(this);
		} else {
			QueryFace temp = queryMap.get(newQuery.getId());
			if (newQuery.equals(temp) && temp != newQuery) {
				removeQuery(temp);
				this.queryMap.put(newQuery.getId(), newQuery);
				newQuery.setConnect(this);
			}
		}
	}

	public void removeQuery(QueryFace oldQuery) {
		if (oldQuery == null)
			return;
		if (this.queryMap != null && this.queryMap.containsKey(oldQuery.getId())) {
			QueryFace temp = queryMap.get(oldQuery.getId());
			if (oldQuery.equals(temp) && temp != oldQuery) {
				temp.setConnect(null);
			}
			this.queryMap.remove(oldQuery.getId());
			oldQuery.setConnect(null);
		}
	}

	public void removeAllQuery() {
		if (queryMap != null) {
			QueryFace oldQuery;
			for (Iterator<? extends QueryFace> iter = getIteratorQuery(); iter.hasNext();) {
				oldQuery = iter.next();
				iter.remove();
				oldQuery.setConnect(null);
			}
		}
	}

	public User getUser() {
		return user;
	}

	@Override
	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeConnect(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addConnect(this);
			}
		}
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUrl() {
		return new StringBuilder("jdbc:iotdb://").append(host).append(":").append(port).toString();
	}

}
