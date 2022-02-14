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
package org.apache.iotdb.ui.config.tsdatasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicTSDataSource extends AbstractRoutingDataSource {

	private String defaultDataSourceName;

	private DataSource defaultDataSource;

	private Map<Object, DataSource> dataSourceMap = new ConcurrentHashMap<>(8);

	private Map<Object, DataSource> getDataSourceMap() {
		return dataSourceMap;
	}

	public DataSource getDefaultDataSource() {
		return defaultDataSource;
	}

	public String getDefaultDataSourceName() {
		return defaultDataSourceName;
	}

	public void registerDefaultDataSource(String name, DataSource dataSource) {
		defaultDataSourceName = name;
		defaultDataSource = dataSource;
	}

	@Override
	public DataSource determineTargetDataSource() {
		Object key = determineCurrentLookupKey();
		if (key.equals(getDefaultDataSourceName())) {
			return getDefaultDataSource();
		} else {
			DataSource ret = getDataSourceMap().get(key);
			if (ret == null) {
				throw new RuntimeException("请先指定数据源");
			}
			return ret;
		}
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return DynamicTSDataSourceContextHolder.getSessionPoolKey();
	}

	public void setDefaultDataSource(Object defaultDataSource) {
		super.setDefaultTargetDataSource(defaultDataSource);
	}

	/**
	 * 设置数据源
	 * 
	 * @param dataSources
	 */
	private void setDataSources(Map<Object, DataSource> dataSources) {
		Map<Object, Object> temp = new HashMap<>(dataSources.size());
		temp.putAll(dataSources);
		super.setTargetDataSources(temp);
	}

	public void loadDataSources() {
		setDataSources(dataSourceMap);
	}

	public DataSource getDataSource(Object name) {
		return dataSourceMap.get(name);
	}

	public void addDataSource(Object name, DataSource ds) {
		dataSourceMap.put(name, ds);
		setDataSources(dataSourceMap);
	}

	public void removeDataSource(Object name) {
		dataSourceMap.remove(name);
		setDataSources(dataSourceMap);
	}
}
