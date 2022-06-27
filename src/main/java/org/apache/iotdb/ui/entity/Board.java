package org.apache.iotdb.ui.entity;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.BoardFace;
import org.apache.iotdb.ui.face.MonitorFace;
import org.apache.iotdb.ui.face.UserFace;

import com.alibaba.fastjson.annotation.JSONField;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_board")
public class Board extends PojoSupport implements BoardFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 名称
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "name", jdbcType = JdbcType.VARCHAR)
	private String name;

	/**
	 * 设置
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "setting", jdbcType = JdbcType.VARCHAR)
	private String setting;

	@FieldMapperAnnotation(dbFieldName = "token", jdbcType = JdbcType.VARCHAR)
	private String token;

	@FieldMapperAnnotation(dbFieldName = "create_time", jdbcType = JdbcType.TIMESTAMP)
	private Date createTime;

	@FieldMapperAnnotation(dbFieldName = "update_time", jdbcType = JdbcType.TIMESTAMP)
	private Date updateTime;

	@JSONField(serialize = false)
	public Map<Object, MonitorFace> monitorMap;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	public Map<Object, ? extends MonitorFace> getMonitorMap() {
		if (monitorMap == null)
			monitorMap = new LinkedHashMap<Object, MonitorFace>();
		return monitorMap;
	}

	public Collection<? extends MonitorFace> getMonitor() {
		return getMonitorMap().values();
	}

	public Iterator<? extends MonitorFace> getIteratorMonitor() {
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
			newMonitor.setBoard(this);
		} else {
			MonitorFace temp = monitorMap.get(newMonitor.getId());
			if (newMonitor.equals(temp) && temp != newMonitor) {
				removeMonitor(temp);
				this.monitorMap.put(newMonitor.getId(), newMonitor);
				newMonitor.setBoard(this);
			}
		}
	}

	public void removeMonitor(MonitorFace oldMonitor) {
		if (oldMonitor == null)
			return;
		if (this.monitorMap != null && this.monitorMap.containsKey(oldMonitor.getId())) {
			MonitorFace temp = monitorMap.get(oldMonitor.getId());
			if (oldMonitor.equals(temp) && temp != oldMonitor) {
				temp.setBoard(null);
			}
			this.monitorMap.remove(oldMonitor.getId());
			oldMonitor.setBoard(null);
		}
	}

	public void removeAllMonitor() {
		if (monitorMap != null) {
			MonitorFace oldMonitor;
			for (Iterator<? extends MonitorFace> iter = getIteratorMonitor(); iter.hasNext();) {
				oldMonitor = iter.next();
				iter.remove();
				oldMonitor.setBoard(null);
			}
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeBoard(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addBoard(this);
			}
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSetting() {
		return setting;
	}

	public void setSetting(String setting) {
		this.setting = setting;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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
