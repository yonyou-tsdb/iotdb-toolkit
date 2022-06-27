package org.apache.iotdb.ui.face;

import org.apache.iotdb.ui.entity.helper.PojoFace;

public interface MonitorFace extends PojoFace {

	void setUser(UserFace newUser);

	void setBoard(BoardFace newBoard);
}
