package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Monitor;

public interface MonitorDao {

	public int insert(Monitor t);

	public Monitor select(Long id);

	public List<Monitor> selectAll(Monitor t);

	public Monitor selectOne(Monitor t);

	public int update(Monitor t);

	public int updatePersistent(Monitor t);

	public int delete(Monitor t);

	public int count(Monitor t);

}
