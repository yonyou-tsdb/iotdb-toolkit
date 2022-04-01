package org.apache.iotdb.ui.service;

import org.apache.iotdb.ui.entity.EmailLog;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.mapper.EmailLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailLogService {

	@Autowired
	private EmailLogDao emailLogDao;

	public void loadUser(User p, EmailLog t) {
		p.removeAllEmailLog();
		t.setUser(p);
		p.setEmailLog(emailLogDao.selectAll(t));
	}

}
