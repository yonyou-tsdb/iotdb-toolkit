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
package org.apache.iotdb.ui.config.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

public class UsernamePasswordIdToken extends UsernamePasswordToken {

	private static final long serialVersionUID = 1L;

	private String userId;

	private String hashPassword;

	private String originPassword;

	public UsernamePasswordIdToken() {
	}

	public UsernamePasswordIdToken(String userId) {
		this.userId = userId;
	}

	public UsernamePasswordIdToken(String username, String password, String userId, String hashPassword) {
		super(username, password);
		this.userId = userId;
		this.hashPassword = hashPassword;
		this.originPassword = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getHashPassword() {
		return hashPassword;
	}

	public void setHashPassword(String hashPassword) {
		this.hashPassword = hashPassword;
	}

	public String getOriginPassword() {
		return originPassword;
	}

	public void setOriginPassword(String originPassword) {
		this.originPassword = originPassword;
	}

}
