package org.apache.iotdb.ui.face;

import org.apache.iotdb.ui.entity.helper.PojoFace;

public interface TaskFace extends PojoFace {
	void setUser(UserFace newUser);
}
