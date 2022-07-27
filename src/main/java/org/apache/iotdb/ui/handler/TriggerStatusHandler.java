package org.apache.iotdb.ui.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.iotdb.ui.model.TriggerStatus;

public class TriggerStatusHandler extends BaseTypeHandler<TriggerStatus> implements TypeHandler<TriggerStatus> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TriggerStatus parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.getValue());
	}

	@Override
	public TriggerStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return TriggerStatus.forValue(rs.getString(columnName));
	}

	@Override
	public TriggerStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return TriggerStatus.forValue(rs.getString(columnIndex));
	}

	@Override
	public TriggerStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return TriggerStatus.forValue(cs.getString(columnIndex));
	}

}
