package org.apache.iotdb.ui.entity;

import java.util.Date;

import org.apache.ibatis.type.JdbcType;
import org.apache.iotdb.ui.entity.helper.PojoSupport;
import org.apache.iotdb.ui.face.BoardFace;
import org.apache.iotdb.ui.face.MonitorFace;
import org.apache.iotdb.ui.face.UserFace;

import indi.mybatis.flying.annotations.FieldMapperAnnotation;
import indi.mybatis.flying.annotations.TableMapperAnnotation;

@TableMapperAnnotation(tableName = "tb_monitor")
public class Monitor extends PojoSupport implements MonitorFace {

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

	public User getUser() {
		return user;
	}

	public void setUser(UserFace newUser) {
		if (this.user == null || this.user != newUser) {
			if (this.user != null) {
				User oldUser = this.user;
				this.user = null;
				oldUser.removeMonitor(this);
			}
			if (newUser != null) {
				this.user = (User) newUser;
				this.user.addMonitor(this);
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
				oldBoard.removeMonitor(this);
			}
			if (newBoard != null) {
				this.board = (Board) newBoard;
				this.board.addMonitor(this);
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

}
