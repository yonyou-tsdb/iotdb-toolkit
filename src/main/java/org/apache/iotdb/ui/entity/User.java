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
import org.apache.iotdb.ui.face.AlertFace;
import org.apache.iotdb.ui.face.BoardFace;
import org.apache.iotdb.ui.face.ConnectFace;
import org.apache.iotdb.ui.face.EmailLogFace;
import org.apache.iotdb.ui.face.ExporterFace;
import org.apache.iotdb.ui.face.MonitorFace;
import org.apache.iotdb.ui.face.TriggerFace;
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

	@JSONField(serialize = false)
	public Map<Object, TriggerFace> triggerMap;

	@JSONField(serialize = false)
	public Map<Object, AlertFace> alertMap;

	@JSONField(serialize = false)
	public Map<Object, ExporterFace> exporterMap;

	@JSONField(serialize = false)
	public Map<Object, MonitorFace> monitorMap;

	@JSONField(serialize = false)
	public Map<Object, BoardFace> boardMap;

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

	public Map<Object, ? extends TriggerFace> getTriggerMap() {
		if (triggerMap == null)
			triggerMap = new LinkedHashMap<Object, TriggerFace>();
		return triggerMap;
	}

	public Collection<? extends TriggerFace> getTrigger() {
		return getTriggerMap().values();
	}

	private Iterator<? extends TriggerFace> getIteratorTrigger() {
		return getTrigger().iterator();
	}

	public void setTrigger(Collection<? extends TriggerFace> newTrigger) {
		removeAllTrigger();
		for (Iterator<? extends TriggerFace> iter = newTrigger.iterator(); iter.hasNext();)
			addTrigger(iter.next());
	}

	public void addTrigger(TriggerFace newTrigger) {
		if (newTrigger == null)
			return;
		if (this.triggerMap == null)
			this.triggerMap = new LinkedHashMap<Object, TriggerFace>();
		if (!this.triggerMap.containsKey(newTrigger.getId())) {
			this.triggerMap.put(newTrigger.getId(), newTrigger);
			newTrigger.setUser(this);
		} else {
			TriggerFace temp = triggerMap.get(newTrigger.getId());
			if (newTrigger.equals(temp) && temp != newTrigger) {
				removeTrigger(temp);
				this.triggerMap.put(newTrigger.getId(), newTrigger);
				newTrigger.setUser(this);
			}
		}
	}

	public void removeTrigger(TriggerFace oldTrigger) {
		if (oldTrigger == null)
			return;
		if (this.triggerMap != null && this.triggerMap.containsKey(oldTrigger.getId())) {
			TriggerFace temp = triggerMap.get(oldTrigger.getId());
			if (oldTrigger.equals(temp) && temp != oldTrigger) {
				temp.setUser(null);
			}
			this.triggerMap.remove(oldTrigger.getId());
			oldTrigger.setUser(null);
		}
	}

	public void removeAllTrigger() {
		if (triggerMap != null) {
			TriggerFace oldTrigger;
			for (Iterator<? extends TriggerFace> iter = getIteratorTrigger(); iter.hasNext();) {
				oldTrigger = iter.next();
				iter.remove();
				oldTrigger.setUser(null);
			}
		}
	}

	public Map<Object, ? extends AlertFace> getAlertMap() {
		if (alertMap == null)
			alertMap = new LinkedHashMap<Object, AlertFace>();
		return alertMap;
	}

	public Collection<? extends AlertFace> getAlert() {
		return getAlertMap().values();
	}

	private Iterator<? extends AlertFace> getIteratorAlert() {
		return getAlert().iterator();
	}

	public void setAlert(Collection<? extends AlertFace> newAlert) {
		removeAllAlert();
		for (Iterator<? extends AlertFace> iter = newAlert.iterator(); iter.hasNext();)
			addAlert(iter.next());
	}

	public void addAlert(AlertFace newAlert) {
		if (newAlert == null)
			return;
		if (this.alertMap == null)
			this.alertMap = new LinkedHashMap<Object, AlertFace>();
		if (!this.alertMap.containsKey(newAlert.getId())) {
			this.alertMap.put(newAlert.getId(), newAlert);
			newAlert.setUser(this);
		} else {
			AlertFace temp = alertMap.get(newAlert.getId());
			if (newAlert.equals(temp) && temp != newAlert) {
				removeAlert(temp);
				this.alertMap.put(newAlert.getId(), newAlert);
				newAlert.setUser(this);
			}
		}
	}

	public void removeAlert(AlertFace oldAlert) {
		if (oldAlert == null)
			return;
		if (this.alertMap != null && this.alertMap.containsKey(oldAlert.getId())) {
			AlertFace temp = alertMap.get(oldAlert.getId());
			if (oldAlert.equals(temp) && temp != oldAlert) {
				temp.setUser(null);
			}
			this.alertMap.remove(oldAlert.getId());
			oldAlert.setUser(null);
		}
	}

	public void removeAllAlert() {
		if (alertMap != null) {
			AlertFace oldAlert;
			for (Iterator<? extends AlertFace> iter = getIteratorAlert(); iter.hasNext();) {
				oldAlert = iter.next();
				iter.remove();
				oldAlert.setUser(null);
			}
		}
	}

	public Map<Object, ? extends ExporterFace> getExporterMap() {
		if (exporterMap == null)
			exporterMap = new LinkedHashMap<Object, ExporterFace>();
		return exporterMap;
	}

	public Collection<? extends ExporterFace> getExporter() {
		return getExporterMap().values();
	}

	private Iterator<? extends ExporterFace> getIteratorExporter() {
		return getExporter().iterator();
	}

	public void setExporter(Collection<? extends ExporterFace> newExporter) {
		removeAllExporter();
		for (Iterator<? extends ExporterFace> iter = newExporter.iterator(); iter.hasNext();)
			addExporter(iter.next());
	}

	public void addExporter(ExporterFace newExporter) {
		if (newExporter == null)
			return;
		if (this.exporterMap == null)
			this.exporterMap = new LinkedHashMap<Object, ExporterFace>();
		if (!this.exporterMap.containsKey(newExporter.getId())) {
			this.exporterMap.put(newExporter.getId(), newExporter);
			newExporter.setUser(this);
		} else {
			ExporterFace temp = exporterMap.get(newExporter.getId());
			if (newExporter.equals(temp) && temp != newExporter) {
				removeExporter(temp);
				this.exporterMap.put(newExporter.getId(), newExporter);
				newExporter.setUser(this);
			}
		}
	}

	public void removeExporter(ExporterFace oldExporter) {
		if (oldExporter == null)
			return;
		if (this.exporterMap != null && this.exporterMap.containsKey(oldExporter.getId())) {
			ExporterFace temp = exporterMap.get(oldExporter.getId());
			if (oldExporter.equals(temp) && temp != oldExporter) {
				temp.setUser(null);
			}
			this.exporterMap.remove(oldExporter.getId());
			oldExporter.setUser(null);
		}
	}

	public void removeAllExporter() {
		if (exporterMap != null) {
			ExporterFace oldExporter;
			for (Iterator<? extends ExporterFace> iter = getIteratorExporter(); iter.hasNext();) {
				oldExporter = iter.next();
				iter.remove();
				oldExporter.setUser(null);
			}
		}
	}

	public Map<Object, ? extends MonitorFace> getMonitorMap() {
		if (monitorMap == null)
			monitorMap = new LinkedHashMap<Object, MonitorFace>();
		return monitorMap;
	}

	public Collection<? extends MonitorFace> getMonitor() {
		return getMonitorMap().values();
	}

	private Iterator<? extends MonitorFace> getIteratorMonitor() {
		return getMonitor().iterator();
	}

	public void setMonitor(Collection<? extends MonitorFace> newMonitor) {
		removeAllMonitor();
		for (Iterator<? extends MonitorFace> iter = newMonitor.iterator(); iter.hasNext();)
			addMonitor(iter.next());
	}

	public void addMonitor(MonitorFace newMonitor) {
		if (newMonitor == null)
			return;
		if (this.monitorMap == null)
			this.monitorMap = new LinkedHashMap<Object, MonitorFace>();
		if (!this.monitorMap.containsKey(newMonitor.getId())) {
			this.monitorMap.put(newMonitor.getId(), newMonitor);
			newMonitor.setUser(this);
		} else {
			MonitorFace temp = monitorMap.get(newMonitor.getId());
			if (newMonitor.equals(temp) && temp != newMonitor) {
				removeMonitor(temp);
				this.monitorMap.put(newMonitor.getId(), newMonitor);
				newMonitor.setUser(this);
			}
		}
	}

	public void removeMonitor(MonitorFace oldMonitor) {
		if (oldMonitor == null)
			return;
		if (this.monitorMap != null && this.monitorMap.containsKey(oldMonitor.getId())) {
			MonitorFace temp = monitorMap.get(oldMonitor.getId());
			if (oldMonitor.equals(temp) && temp != oldMonitor) {
				temp.setUser(null);
			}
			this.monitorMap.remove(oldMonitor.getId());
			oldMonitor.setUser(null);
		}
	}

	public void removeAllMonitor() {
		if (monitorMap != null) {
			MonitorFace oldMonitor;
			for (Iterator<? extends MonitorFace> iter = getIteratorMonitor(); iter.hasNext();) {
				oldMonitor = iter.next();
				iter.remove();
				oldMonitor.setUser(null);
			}
		}
	}

	public Map<Object, ? extends BoardFace> getBoardMap() {
		if (boardMap == null)
			boardMap = new LinkedHashMap<Object, BoardFace>();
		return boardMap;
	}

	public Collection<? extends BoardFace> getBoard() {
		return getBoardMap().values();
	}

	private Iterator<? extends BoardFace> getIteratorBoard() {
		return getBoard().iterator();
	}

	public void setBoard(Collection<? extends BoardFace> newBoard) {
		removeAllBoard();
		for (Iterator<? extends BoardFace> iter = newBoard.iterator(); iter.hasNext();)
			addBoard(iter.next());
	}

	public void addBoard(BoardFace newBoard) {
		if (newBoard == null)
			return;
		if (this.boardMap == null)
			this.boardMap = new LinkedHashMap<Object, BoardFace>();
		if (!this.boardMap.containsKey(newBoard.getId())) {
			this.boardMap.put(newBoard.getId(), newBoard);
			newBoard.setUser(this);
		} else {
			BoardFace temp = boardMap.get(newBoard.getId());
			if (newBoard.equals(temp) && temp != newBoard) {
				removeBoard(temp);
				this.boardMap.put(newBoard.getId(), newBoard);
				newBoard.setUser(this);
			}
		}
	}

	public void removeBoard(BoardFace oldBoard) {
		if (oldBoard == null)
			return;
		if (this.boardMap != null && this.boardMap.containsKey(oldBoard.getId())) {
			BoardFace temp = boardMap.get(oldBoard.getId());
			if (oldBoard.equals(temp) && temp != oldBoard) {
				temp.setUser(null);
			}
			this.boardMap.remove(oldBoard.getId());
			oldBoard.setUser(null);
		}
	}

	public void removeAllBoard() {
		if (boardMap != null) {
			BoardFace oldBoard;
			for (Iterator<? extends BoardFace> iter = getIteratorBoard(); iter.hasNext();) {
				oldBoard = iter.next();
				iter.remove();
				oldBoard.setUser(null);
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
