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
import org.apache.iotdb.ui.face.ConnectFace;
import org.apache.iotdb.ui.face.QueryFace;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_query")
public class Query extends PojoSupport implements QueryFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 查询名
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "name", jdbcType = JdbcType.VARCHAR)
	private String name;

	/**
	 * 相关sql语句
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "sqls", jdbcType = JdbcType.VARCHAR)
	private String sqls;

	/**
	 * 创建时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "create_time", jdbcType = JdbcType.TIMESTAMP)
	private Date createTime;

	@FieldMapperAnnotation(dbFieldName = "connect_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private Connect connect;

	@FieldMapperAnnotation(dbFieldName = "connect_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long connectId;

	public Connect getConnect() {
		return connect;
	}

	@Override
	public void setConnect(ConnectFace newConnect) {
		if (this.connect == null || this.connect != newConnect) {
			if (this.connect != null) {
				Connect oldConnect = this.connect;
				this.connect = null;
				oldConnect.removeQuery(this);
			}
			if (newConnect != null) {
				this.connect = (Connect) newConnect;
				this.connect.addQuery(this);
			}
		}
	}

	public Long getConnectId() {
		return connectId;
	}

	public void setConnectId(Long connectId) {
		this.connectId = connectId;
	}

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

	public String getSqls() {
		return sqls;
	}

	public void setSqls(String sqls) {
		this.sqls = sqls;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
