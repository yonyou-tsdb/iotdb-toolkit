package org.apache.iotdb.ui.face;

import org.apache.iotdb.ui.entity.helper.PojoFace;

public interface TriggerFace extends PojoFace {

	void setUser(UserFace newUser);

	void setAlert(AlertFace newAlert);

}
