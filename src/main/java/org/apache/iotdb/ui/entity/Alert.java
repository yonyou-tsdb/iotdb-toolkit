package org.apache.iotdb.ui.entity;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.AlertFace;
import org.apache.iotdb.ui.face.TriggerFace;
import org.apache.iotdb.ui.face.UserFace;
import org.apache.iotdb.ui.handler.AlertStatusHandler;
import org.apache.iotdb.ui.model.AlertStatus;

import com.alibaba.fastjson.annotation.JSONField;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_alert")
public class Alert extends PojoSupport implements AlertFace {

	/**
	 * 主键
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "id", jdbcType = JdbcType.BIGINT, isUniqueKey = true)
	private Long id;

	/**
	 * 来源相同的alert的code保持不变
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "code", jdbcType = JdbcType.VARCHAR)
	private String code;

	/**
	 * 版本，每次部署后再编辑时增加一条新的alert数据，其version自增，status由“已部署”变为“开发中”
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "version", jdbcType = JdbcType.INTEGER)
	private Integer version;

	/**
	 * 来源，创建alert时origin等于id，编辑新版本时origin保持不变
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "origin", jdbcType = JdbcType.BIGINT)
	private Long origin;

	/**
	 * 创建时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "create_time", jdbcType = JdbcType.TIMESTAMP)
	private Date createTime;

	/**
	 * 修改时间
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "update_time", jdbcType = JdbcType.TIMESTAMP)
	private Date updateTime;

	/**
	 * 状态（0开发中1已部署）
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "status", jdbcType = JdbcType.CHAR, customTypeHandler = AlertStatusHandler.class)
	private AlertStatus status;

	@FieldMapperAnnotation(dbFieldName = "token", jdbcType = JdbcType.VARCHAR)
	private String token;

	/**
	 * 模型
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "rule", jdbcType = JdbcType.VARCHAR)
	private String rule;

	@JSONField(serialize = false)
	private Map<Object, TriggerFace> triggerMap;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	public Map<Object, ? extends TriggerFace> getTriggerMap() {
		if (triggerMap == null)
			triggerMap = new LinkedHashMap<Object, TriggerFace>();
		return triggerMap;
	}

	public Collection<? extends TriggerFace> getTrigger() {
		return getTriggerMap().values();
	}

	public Iterator<? extends TriggerFace> getIteratorTrigger() {
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
			newTrigger.setAlert(this);
		} else {
			TriggerFace temp = triggerMap.get(newTrigger.getId());
			if (newTrigger.equals(temp) && temp != newTrigger) {
				removeTrigger(temp);
				this.triggerMap.put(newTrigger.getId(), newTrigger);
				newTrigger.setAlert(this);
			}
		}
	}

	public void removeTrigger(TriggerFace oldTrigger) {
		if (oldTrigger == null)
			return;
		if (this.triggerMap != null && this.triggerMap.containsKey(oldTrigger.getId())) {
			TriggerFace temp = triggerMap.get(oldTrigger.getId());
			if (oldTrigger.equals(temp) && temp != oldTrigger) {
				temp.setAlert(null);
			}
			this.triggerMap.remove(oldTrigger.getId());
			oldTrigger.setAlert(null);
		}
	}

	public void removeAllTrigger() {
		if (triggerMap != null) {
			TriggerFace oldTrigger;
			for (Iterator<? extends TriggerFace> iter = getIteratorTrigger(); iter.hasNext();) {
				oldTrigger = iter.next();
				iter.remove();
				oldTrigger.setAlert(null);
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
				oldUser.removeAlert(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addAlert(this);
			}
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getOrigin() {
		return origin;
	}

	public void setOrigin(Long origin) {
		this.origin = origin;
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

	public AlertStatus getStatus() {
		return status;
	}

	public void setStatus(AlertStatus status) {
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
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
