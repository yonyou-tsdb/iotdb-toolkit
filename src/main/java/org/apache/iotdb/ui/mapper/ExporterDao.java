package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Exporter;

public interface ExporterDao {

	public int insert(Exporter t);

	public Exporter select(Long id);

	public List<Exporter> selectAll(Exporter t);

	public Exporter selectOne(Exporter t);

	public int update(Exporter t);

	public int updatePersistent(Exporter t);

	public int delete(Exporter t);

	public int count(Exporter t);

}
