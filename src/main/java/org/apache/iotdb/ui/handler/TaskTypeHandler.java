package org.apache.iotdb.ui.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.iotdb.ui.model.TaskType;

public class TaskTypeHandler extends BaseTypeHandler<TaskType> implements TypeHandler<TaskType> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TaskType parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.getValue());
	}

	@Override
	public TaskType getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return TaskType.forValue(rs.getString(columnName));
	}

	@Override
	public TaskType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return TaskType.forValue(rs.getString(columnIndex));
	}

	@Override
	public TaskType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return TaskType.forValue(cs.getString(columnIndex));
	}

}
