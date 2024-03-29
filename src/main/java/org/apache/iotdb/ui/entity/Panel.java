package org.apache.iotdb.ui.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.BoardFace;
import org.apache.iotdb.ui.face.PanelFace;
import org.apache.iotdb.ui.face.UserFace;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_panel")
public class Panel extends PojoSupport implements PanelFace {

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
	 * 查询sql
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "query", jdbcType = JdbcType.VARCHAR)
	private String query;

	/**
	 * 刷新周期，单位秒
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "period", jdbcType = JdbcType.INTEGER)
	private Integer period;

	/**
	 * 显示顺序
	 * 
	 */
	@FieldMapperAnnotation(dbFieldName = "display_order", jdbcType = JdbcType.INTEGER)
	private Integer displayOrder;

	@FieldMapperAnnotation(dbFieldName = "setting", jdbcType = JdbcType.VARCHAR)
	private String setting;

	@FieldMapperAnnotation(dbFieldName = "create_time", jdbcType = JdbcType.TIMESTAMP)
	private Date createTime;

	@FieldMapperAnnotation(dbFieldName = "update_time", jdbcType = JdbcType.TIMESTAMP)
	private Date updateTime;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private User user;

	@FieldMapperAnnotation(dbFieldName = "user_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long userId;

	@FieldMapperAnnotation(dbFieldName = "board_id", jdbcType = JdbcType.BIGINT, dbAssociationUniqueKey = "id")
	private Board board;

	@FieldMapperAnnotation(dbFieldName = "board_id", jdbcType = JdbcType.BIGINT, delegate = true)
	private Long boardId;

	private List<Map<String, Object>> monitorDataList;

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removePanel(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addPanel(this);
			}
		}
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(BoardFace newBoard) {
		if (this.board == null || this.board != newBoard) {
			if (this.board != null) {
				Board oldBoard = this.board;
				this.board = null;
				oldBoard.removePanel(this);
			}
			if (newBoard != null) {
				this.board = (Board) newBoard;
				this.board.addPanel(this);
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

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getSetting() {
		return setting;
	}

	public void setSetting(String setting) {
		this.setting = setting;
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

	public Long getBoardId() {
		return boardId;
	}

	public void setBoardId(Long boardId) {
		this.boardId = boardId;
	}

	public List<Map<String, Object>> getMonitorDataList() {
		return monitorDataList;
	}

	public void setMonitorDataList(List<Map<String, Object>> monitorDataList) {
		this.monitorDataList = monitorDataList;
	}

}
