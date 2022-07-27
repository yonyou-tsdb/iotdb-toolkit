/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.ui.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.iotdb.ui.model.AlertStatus;

public class AlertStatusHandler extends BaseTypeHandler<AlertStatus> implements TypeHandler<AlertStatus> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, AlertStatus parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.getValue());
	}

	@Override
	public AlertStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return AlertStatus.forValue(rs.getString(columnName));
	}

	@Override
	public AlertStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return AlertStatus.forValue(rs.getString(columnIndex));
	}

	@Override
	public AlertStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return AlertStatus.forValue(cs.getString(columnIndex));
	}

}
