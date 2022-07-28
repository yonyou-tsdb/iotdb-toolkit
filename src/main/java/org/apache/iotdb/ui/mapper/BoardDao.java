package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Board;

public interface BoardDao {

	public int insert(Board t);

	public Board select(Long id);

	public List<Board> selectAll(Board t);

	public Board selectOne(Board t);

	public int update(Board t);

	public int updatePersistent(Board t);

	public int delete(Board t);

	public int count(Board t);

}
