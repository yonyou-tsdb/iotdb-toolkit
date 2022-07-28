package org.apache.iotdb.ui.service;

import org.apache.iotdb.ui.entity.Alert;
import org.apache.iotdb.ui.entity.Trigger;
import org.apache.iotdb.ui.mapper.TriggerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TriggerService {

	@Autowired
	private TriggerDao triggerDao;

	public void loadAlert(Alert p, Trigger t) {
		p.removeAllTrigger();
		t.setAlert(p);
		p.setTrigger(triggerDao.selectAll(t));
	}

}
