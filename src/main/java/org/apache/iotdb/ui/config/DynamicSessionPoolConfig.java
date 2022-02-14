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
package org.apache.iotdb.ui.config;

import java.sql.SQLException;
import java.util.List;

import org.apache.iotdb.ui.config.tsdatasource.DynamicSessionPool;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class DynamicSessionPoolConfig {

	public static final int MAX_SIZE = 100;

	@Autowired
	private ConnectDao connectDao;

	protected static final Logger LOGGER = LoggerFactory.getLogger(DynamicSessionPoolConfig.class);

	@Bean("dynamicSessionPool")
	public DynamicSessionPool dynamicSessionPool() {
		LOGGER.error("=========DynamicSessionPoolBegin==========");
		DynamicSessionPool dynamicSessionPool = new DynamicSessionPool();
		// In real case, time series datasource are loaded from the data in the
		// database, so dynamicTSDataSource initializes later than dataSource
		LOGGER.error("=========DynamicSessionPoolEnd==========");
		return dynamicSessionPool;
	}

	@Bean("postDynamicSessionPool")
	@DependsOn("dynamicSessionPool")
	public String postDynamicSessionPool(DynamicSessionPool dynamicSessionPool) throws SQLException {
		LOGGER.error("=========PostDynamicSessionPoolBegin==========");
		// Load all time series datasource tenant that be found in database
		loadTenantSessionPool(dynamicSessionPool);
		LOGGER.error("=========PostDynamicSessionPoolEnd==========");
		return "done";
	}

	private void loadTenantSessionPool(DynamicSessionPool dynamicSessionPool) throws SQLException {
		Connect c = new Connect();
		List<Connect> list = connectDao.selectAll(c);
		for (Connect e : list) {
			dynamicSessionPool.addSessionPool(e.getId(), e.getHost(), e.getPort(), e.getUsername(), e.getPassword(),
					MAX_SIZE);
		}
	}
}
