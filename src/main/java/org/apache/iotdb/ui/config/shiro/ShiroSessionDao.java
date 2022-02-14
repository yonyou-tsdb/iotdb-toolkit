package org.apache.iotdb.ui.config.shiro;

import java.io.Serializable;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;

public class ShiroSessionDao extends EnterpriseCacheSessionDAO {

	@Override
	protected Serializable doCreate(Session session) {
		String id = super.doCreate(session).toString();
		id = id.replaceAll("-", "");
		assignSessionId(session, id);
		return id;
	}

}
