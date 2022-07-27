package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Trigger;

public interface TriggerDao {

	public int insert(Trigger t);

	public Trigger select(Long id);

	public List<Trigger> selectAll(Trigger t);

	public Trigger selectOne(Trigger t);

	public int update(Trigger t);

	public int updatePersistent(Trigger t);

	public int delete(Trigger t);

	public int count(Trigger t);

}
