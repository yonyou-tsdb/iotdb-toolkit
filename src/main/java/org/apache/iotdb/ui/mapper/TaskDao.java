package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Task;

public interface TaskDao {

	public int insert(Task t);

	public Task select(Long id);

	public List<Task> selectAll(Task t);
	
	public List<Task> selectAllPure(Task t);

	public Task selectOne(Task t);
	
	public Task selectOnePure(Task t);

	public int update(Task t);

	public int updatePersistent(Task t);

	public int delete(Task t);

	public int count(Task t);

}
