package org.apache.iotdb.ui.condition;

import org.apache.iotdb.ui.entity.Panel;

import indi.mybatis.flying.annotations.ConditionMapperAnnotation;
import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.models.Limitable;
import indi.mybatis.flying.models.Sortable;
import indi.mybatis.flying.statics.ConditionType;

public class PanelCondition extends Panel implements Conditionable {

	private Limitable limiter;

	private Sortable sorter;

	@ConditionMapperAnnotation(dbFieldName = "board_id", conditionType = ConditionType.EQUAL)
	private Long boardIdEqual;

	@ConditionMapperAnnotation(dbFieldName = "name", conditionType = ConditionType.LIKE)
	private String nameLike;

	public Limitable getLimiter() {
		return limiter;
	}

	public void setLimiter(Limitable limiter) {
		this.limiter = limiter;
	}

	public Sortable getSorter() {
		return sorter;
	}

	public void setSorter(Sortable sorter) {
		this.sorter = sorter;
	}

	public String getNameLike() {
		return nameLike;
	}

	public void setNameLike(String nameLike) {
		this.nameLike = nameLike;
	}

	public Long getBoardIdEqual() {
		return boardIdEqual;
	}

	public void setBoardIdEqual(Long boardIdEqual) {
		this.boardIdEqual = boardIdEqual;
	}

}
