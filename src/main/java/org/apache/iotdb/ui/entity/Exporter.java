package org.apache.iotdb.ui.entity;

import java.util.Date;

import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.ExporterFace;
import org.apache.iotdb.ui.face.UserFace;

public class Exporter extends PojoSupport implements ExporterFace {

	/**
	 * 主键
	 * 
	 */
	private Long id;
	/**
	 * exporter端点
	 * 
	 */
	private String endPoint;
	/**
	 * 名称
	 * 
	 */
	private String name;
	/**
	 * 业务编码
	 * 
	 */
	private String code;
	/**
	 * 读取周期，单位秒
	 * 
	 */
	private Integer period;

	private Date createTime;

	private Date updateTime;

	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeExporter(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addExporter(this);
			}
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
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
