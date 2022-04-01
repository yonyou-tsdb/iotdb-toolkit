package org.apache.iotdb.ui.face;

import org.apache.iotdb.ui.entity.helper.PojoFace;

public interface EmailLogFace extends PojoFace {
	public void setUser(UserFace newUser);
}
