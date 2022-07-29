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
package org.apache.iotdb.ui.mapper;

import java.util.List;

import org.apache.iotdb.ui.entity.Connect;

public interface ConnectDao {

	public int insert(Connect t);

	public Connect select(Long id);
	
	public Connect selectWithSetting(Long id);

	public List<Connect> selectAll(Connect t);

	public Connect selectOne(Connect t);
	
	public Connect selectOneWithSetting(Connect t);

	public int update(Connect t);

	public int updatePersistent(Connect t);

	public int delete(Connect t);

	public int count(Connect t);
}
