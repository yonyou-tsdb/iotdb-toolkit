package org.apache.iotdb.ui.entity;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.BoardFace;
import org.apache.iotdb.ui.face.PanelFace;
import org.apache.iotdb.ui.face.PanelFace;
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
	public Map<Object, PanelFace> panelMap;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	public Map<Object, ? extends PanelFace> getPanelMap() {
		if (panelMap == null)
			panelMap = new LinkedHashMap<Object, PanelFace>();
		return panelMap;
	}

	public Collection<? extends PanelFace> getPanel() {
		return getPanelMap().values();
	}

	private Iterator<? extends PanelFace> getIteratorPanel() {
		return getPanel().iterator();
	}

	public void setPanel(Collection<? extends PanelFace> newPanel) {
		removeAllPanel();
		for (Iterator<? extends PanelFace> iter = newPanel.iterator(); iter.hasNext();)
			addPanel(iter.next());
	}

	public void addPanel(PanelFace newPanel) {
		if (newPanel == null)
			return;
		if (this.panelMap == null)
			this.panelMap = new LinkedHashMap<Object, PanelFace>();
		if (!this.panelMap.containsKey(newPanel.getId())) {
			this.panelMap.put(newPanel.getId(), newPanel);
			newPanel.setBoard(this);
		} else {
			PanelFace temp = panelMap.get(newPanel.getId());
			if (newPanel.equals(temp) && temp != newPanel) {
				removePanel(temp);
				this.panelMap.put(newPanel.getId(), newPanel);
				newPanel.setBoard(this);
			}
		}
	}

	public void removePanel(PanelFace oldPanel) {
		if (oldPanel == null)
			return;
		if (this.panelMap != null && this.panelMap.containsKey(oldPanel.getId())) {
			PanelFace temp = panelMap.get(oldPanel.getId());
			if (oldPanel.equals(temp) && temp != oldPanel) {
				temp.setBoard(null);
			}
			this.panelMap.remove(oldPanel.getId());
			oldPanel.setBoard(null);
		}
	}

	public void removeAllPanel() {
		if (panelMap != null) {
			PanelFace oldPanel;
			for (Iterator<? extends PanelFace> iter = getIteratorPanel(); iter.hasNext();) {
				oldPanel = iter.next();
				iter.remove();
				oldPanel.setBoard(null);
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
