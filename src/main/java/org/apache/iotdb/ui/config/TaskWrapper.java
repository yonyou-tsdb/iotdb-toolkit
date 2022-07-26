package org.apache.iotdb.ui.config;

import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.model.TaskType;

import com.yonyou.iotdb.utils.core.ExportStarter;
import com.yonyou.iotdb.utils.core.ImportStarter;
import com.yonyou.iotdb.utils.core.pipeline.context.model.ExportModel;
import com.yonyou.iotdb.utils.core.pipeline.context.model.ImportModel;

import reactor.core.Disposable;

public class TaskWrapper {

	private Task task;

	private ExportStarter exportStarter;

	private ImportStarter importStarter;

	private Disposable disposable;

	public boolean isFinish() {
		if (task == null || TaskType.EXPORT.equals(task.getType()) || TaskType.IMPORT.equals(task.getType())) {
			return disposable == null ? false : disposable.isDisposed();
		}
		return false;
	}

	public Long getProcess() {
		if (task != null) {
			if (TaskType.EXPORT.equals(task.getType())) {
				return exportStarter.finishedRowNum();
			} else if (TaskType.IMPORT.equals(task.getType())) {
				return importStarter.finishedRowNum();
			}
		}
		return 0L;
	}

	public Disposable start(ExportModel exportModel) {
		disposable = exportStarter.start(exportModel);
		return disposable;
	}

	public Disposable start(ImportModel importModel) {
		disposable = importStarter.start(importModel);
		return disposable;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public ExportStarter getExportStarter() {
		return exportStarter;
	}

	public void setExportStarter(ExportStarter exportStarter) {
		this.exportStarter = exportStarter;
	}

	public ImportStarter getImportStarter() {
		return importStarter;
	}

	public void setImportStarter(ImportStarter importStarter) {
		this.importStarter = importStarter;
	}

	public Disposable getDisposable() {
		return disposable;
	}

	public void setDisposable(Disposable disposable) {
		this.disposable = disposable;
	}

}
