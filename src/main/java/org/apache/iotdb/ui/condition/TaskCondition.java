package org.apache.iotdb.ui.condition;

import org.apache.iotdb.ui.entity.Task;

import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.models.Limitable;
import indi.mybatis.flying.models.Sortable;

public class TaskCondition extends Task implements Conditionable {

	private Limitable limiter;

	private Sortable sorter;

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
}
