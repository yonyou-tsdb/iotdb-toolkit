package org.apache.iotdb.ui.entity;

import java.util.Date;

import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.AlertFace;
import org.apache.iotdb.ui.face.TriggerFace;
import org.apache.iotdb.ui.face.UserFace;
import org.apache.iotdb.ui.model.TriggerStatus;

public class Trigger extends PojoSupport implements TriggerFace {

	/**
	 * 主键
	 * 
	 */
	private Long id;
	/**
	 * 名称
	 * 
	 */
	private String name;
	/**
	 * 时间序列
	 * 
	 */
	private String timeseries;
	/**
	 * 状态（0删除1启用2禁用）
	 * 
	 */
	private TriggerStatus status;
	/**
	 * 建立时间
	 * 
	 */
	private Date createTime;
	private Date updateTime;

	private User user;
	private Alert alert;

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeTrigger(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addTrigger(this);
			}
		}
	}

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(AlertFace newAlert) {
		if (this.alert == null || this.alert != newAlert) {
			if (this.alert != null) {
				Alert oldAlert = this.alert;
				this.alert = null;
				oldAlert.removeTrigger(this);
			}
			if (newAlert != null) {
				this.alert = (Alert) newAlert;
				this.alert.addTrigger(this);
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

	public String getTimeseries() {
		return timeseries;
	}

	public void setTimeseries(String timeseries) {
		this.timeseries = timeseries;
	}

	public TriggerStatus getStatus() {
		return status;
	}

	public void setStatus(TriggerStatus status) {
		this.status = status;
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

}
