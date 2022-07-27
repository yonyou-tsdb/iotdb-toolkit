package org.apache.iotdb.ui.service;

import org.apache.iotdb.ui.entity.Board;
import org.apache.iotdb.ui.entity.Panel;
import org.apache.iotdb.ui.mapper.PanelDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PanelService {

	@Autowired
	private PanelDao monitorDao;

	public void loadBoard(Board p, Panel t) {
		p.removeAllPanel();
		t.setBoard(p);
		p.setPanel(monitorDao.selectAll(t));
	}

}
