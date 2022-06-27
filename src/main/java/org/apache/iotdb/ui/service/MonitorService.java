package org.apache.iotdb.ui.service;

import org.apache.iotdb.ui.entity.Board;
import org.apache.iotdb.ui.entity.Monitor;
import org.apache.iotdb.ui.mapper.MonitorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitorService {

	@Autowired
	private MonitorDao monitorDao;

	public void loadBoard(Board p, Monitor t) {
		p.removeAllMonitor();
		t.setBoard(p);
		p.setMonitor(monitorDao.selectAll(t));
	}

}
