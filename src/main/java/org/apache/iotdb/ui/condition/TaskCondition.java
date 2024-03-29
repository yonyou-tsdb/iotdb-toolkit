package org.apache.iotdb.ui.condition;

import java.util.List;

import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.model.TaskStatus;

import indi.mybatis.flying.annotations.ConditionMapperAnnotation;
import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.models.Limitable;
import indi.mybatis.flying.models.Sortable;
import indi.mybatis.flying.statics.ConditionType;

public class TaskCondition extends Task implements Conditionable {

	private Limitable limiter;

	private Sortable sorter;

	@ConditionMapperAnnotation(dbFieldName = "status", conditionType = ConditionType.IN, customTypeHandler = org.apache.iotdb.ui.handler.TaskStatusHandler.class)
	private List<TaskStatus> statusIn;

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

	public List<TaskStatus> getStatusIn() {
		return statusIn;
	}

	public void setStatusIn(List<TaskStatus> statusIn) {
		this.statusIn = statusIn;
	}

}
