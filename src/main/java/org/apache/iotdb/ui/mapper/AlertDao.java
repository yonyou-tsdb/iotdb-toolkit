package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Alert;

public interface AlertDao {

	public int insert(Alert t);

	public Alert select(Long id);

	public List<Alert> selectAll(Alert t);

	public Alert selectOne(Alert t);

	public int update(Alert t);

	public int updatePersistent(Alert t);

	public int delete(Alert t);

	public int count(Alert t);

}
