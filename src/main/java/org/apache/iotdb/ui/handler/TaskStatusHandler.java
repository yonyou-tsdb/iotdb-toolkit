package org.apache.iotdb.ui.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.iotdb.ui.model.TaskStatus;

public class TaskStatusHandler extends BaseTypeHandler<TaskStatus> implements TypeHandler<TaskStatus> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TaskStatus parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.getValue());
	}

	@Override
	public TaskStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return TaskStatus.forValue(rs.getString(columnName));
	}

	@Override
	public TaskStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return TaskStatus.forValue(rs.getString(columnIndex));
	}

	@Override
	public TaskStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return TaskStatus.forValue(cs.getString(columnIndex));
	}

}
