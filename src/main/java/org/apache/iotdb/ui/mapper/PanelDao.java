package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Panel;

public interface PanelDao {

	public int insert(Panel t);

	public Panel select(Long id);

	public List<Panel> selectAll(Panel t);

	public Panel selectOne(Panel t);

	public int update(Panel t);

	public int updatePersistent(Panel t);

	public int delete(Panel t);

	public int count(Panel t);

}
