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

import org.apache.iotdb.ui.entity.User;

public interface UserDao {

	public int insert(User t);

	public User select(Long id);

	public User selectWithEverything(Long id);

	public List<User> selectAll(User t);

	public User selectOne(User t);

	public User selectOneWithEverything(User t);

	public int update(User t);

	public int updatePersistent(User t);

	public int delete(User t);

	public int count(User t);

}
