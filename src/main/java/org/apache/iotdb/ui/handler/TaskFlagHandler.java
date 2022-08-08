package org.apache.iotdb.ui.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.iotdb.ui.model.TaskFlag;

public class TaskFlagHandler extends BaseTypeHandler<TaskFlag> implements TypeHandler<TaskFlag> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TaskFlag parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.getValue());
	}

	@Override
	public TaskFlag getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return TaskFlag.forValue(rs.getString(columnName));
	}

	@Override
	public TaskFlag getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return TaskFlag.forValue(rs.getString(columnIndex));
	}

	@Override
	public TaskFlag getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return TaskFlag.forValue(cs.getString(columnIndex));
	}

}
