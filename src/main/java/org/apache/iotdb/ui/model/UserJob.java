package org.apache.iotdb.ui.model;

import java.util.UUID;

public class UserJob {

	public UserJob() {

	}

	public UserJob(Long id_, JobType type_) {
		id = id_;
		type = type_;
	}

	private Long id;

	private JobType type;

	// 被中断
	private boolean interrupt;

	// 即将结束
	private boolean aboutToFinish;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JobType getType() {
		return type;
	}

	public void setType(JobType type) {
		this.type = type;
	}

	public boolean isInterrupt() {
		return interrupt;
	}

	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}

	public boolean isAboutToFinish() {
		return aboutToFinish;
	}

	public void setAboutToFinish(boolean aboutToFinish) {
		this.aboutToFinish = aboutToFinish;
	}

}
