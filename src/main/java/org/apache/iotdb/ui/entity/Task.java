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
import org.apache.iotdb.ui.face.TaskFace;
import org.apache.iotdb.ui.face.UserFace;
import org.apache.iotdb.ui.handler.TaskStatusHandler;
import org.apache.iotdb.ui.handler.TaskTypeHandler;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.iotdb.ui.model.TaskType;
import org.apache.iotdb.ui.util.CommonUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_task")
public class Task extends PojoSupport implements TaskFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 类型
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "type", jdbcType = JdbcType.CHAR, customTypeHandler = TaskTypeHandler.class)
	private TaskType type;

	/**
	 * 参数设置
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "setting", jdbcType = JdbcType.VARCHAR)
	private JSONObject setting;

	/**
	 * 时间窗口起始时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "start_window_from", jdbcType = JdbcType.TIMESTAMP)
	private Date startWindowFrom;

	/**
	 * 时间窗口结束时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "start_window_to", jdbcType = JdbcType.TIMESTAMP)
	private Date startWindowTo;

	/**
	 * 优先级
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "priority", jdbcType = JdbcType.INTEGER)
	private Integer priority;

	/**
	 * 状态（0未开始1进行中2正常结束3异常结束4强制结束）
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "status", jdbcType = JdbcType.CHAR, customTypeHandler = TaskStatusHandler.class)
	private TaskStatus status;

	/**
	 * 结果行数
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "result_rows", jdbcType = JdbcType.BIGINT)
	private Long resultRows;

	@FieldMapperAnnotation(dbFieldName = "create_time", jdbcType = JdbcType.TIMESTAMP)
	private Date createTime;

	@FieldMapperAnnotation(dbFieldName = "update_time", jdbcType = JdbcType.TIMESTAMP)
	private Date updateTime;

	/**
	 * 名称
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "name", jdbcType = JdbcType.VARCHAR)
	private String name;

	/**
	 * 任务开始时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "start_time", jdbcType = JdbcType.TIMESTAMP)
	private Date startTime;

	/**
	 * 任务结束时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "end_time", jdbcType = JdbcType.TIMESTAMP)
	private Date endTime;

	/**
	 * 任务用时（秒）
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "time_cost", jdbcType = JdbcType.INTEGER)
	private Integer timeCost;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeTask(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addTask(this);
			}
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	public JSONObject getSetting() {
		return setting;
	}

	public void setSetting(JSONObject setting) {
		this.setting = setting;
	}

	public Date getStartWindowFrom() {
		return startWindowFrom;
	}

	public void setStartWindowFrom(Date startWindowFrom) {
		this.startWindowFrom = startWindowFrom;
	}

	public Date getStartWindowTo() {
		return startWindowTo;
	}

	public void setStartWindowTo(Date startWindowTo) {
		this.startWindowTo = startWindowTo;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public Long getResultRows() {
		return resultRows;
	}

	public void setResultRows(Long resultRows) {
		this.resultRows = resultRows;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(Integer timeCost) {
		this.timeCost = timeCost;
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

	@JSONField(serialize = false)
	public String key() {
		return new StringBuilder(CommonUtils.addZeroForNum(startWindowFrom == null ? null : startWindowFrom.getTime()))
				.append(CommonUtils.addZeroForNum(priority)).append(CommonUtils.addZeroForNum(id)).toString();
	}

}
