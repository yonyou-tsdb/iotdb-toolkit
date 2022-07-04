package org.apache.iotdb.ui.condition;

import org.apache.iotdb.ui.entity.Board;

import indi.mybatis.flying.annotations.ConditionMapperAnnotation;
import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.models.Limitable;
import indi.mybatis.flying.models.Sortable;
import indi.mybatis.flying.statics.ConditionType;

public class BoardCondition extends Board implements Conditionable {

	private Limitable limiter;

	private Sortable sorter;

	@ConditionMapperAnnotation(dbFieldName = "id", conditionType = ConditionType.EQUAL)
	private Long idEqual;

	@ConditionMapperAnnotation(dbFieldName = "user_id", conditionType = ConditionType.EQUAL)
	private Long userIdEqual;

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

	public Long getIdEqual() {
		return idEqual;
	}

	public void setIdEqual(Long idEqual) {
		this.idEqual = idEqual;
	}

	public Long getUserIdEqual() {
		return userIdEqual;
	}

	public void setUserIdEqual(Long userIdEqual) {
		this.userIdEqual = userIdEqual;
	}

	public String getNameLike() {
		return nameLike;
	}

	public void setNameLike(String nameLike) {
		this.nameLike = nameLike;
	}

}
