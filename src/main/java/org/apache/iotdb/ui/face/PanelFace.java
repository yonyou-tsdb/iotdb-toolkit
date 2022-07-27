package org.apache.iotdb.ui.face;

import org.apache.iotdb.ui.entity.helper.PojoFace;

public interface PanelFace extends PojoFace {

	void setUser(UserFace newUser);

	void setBoard(BoardFace newBoard);
}
