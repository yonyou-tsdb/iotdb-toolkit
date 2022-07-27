package org.apache.iotdb.ui.config.schedule;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.iotdb.ui.entity.Task;

public class TaskTimerBucket {

	private ConcurrentSkipListMap<String, Task> taskTimerMap = new ConcurrentSkipListMap<String, Task>(
			new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

	public ConcurrentSkipListMap<String, Task> getTaskTimerMap() {
		return taskTimerMap;
	}

	public Task getTaskTimer(Object name) {
		return taskTimerMap.get(name);
	}

	public void addTaskTimer(Task task) {
		taskTimerMap.put(task.getId().toString(), task);
	}

	public void removeTaskTimer(Object key) {
		taskTimerMap.remove(key);
	}

}
