package org.apache.iotdb.ui.face;

import org.apache.iotdb.ui.entity.helper.PojoFace;

public interface AlertFace extends PojoFace {
	void setUser(UserFace newUser);
}
