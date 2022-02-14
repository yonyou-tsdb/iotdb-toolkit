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

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ShiroSession extends SimpleSession implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date expirationTime;

	private Boolean isChanged;

	private String userId;

	public ShiroSession() {
		super();
		this.isChanged = true;
	}

	public ShiroSession(String host) {
		super(host);
		this.isChanged = true;
	}

	public ShiroSession(String userid, Date expirationTime, String host) {
		super(host);
		this.expirationTime = expirationTime;
		this.isChanged = true;
	}

	@Override
	public boolean isValid() {
		return super.isValid();
	}

	@Override
	public void validate() throws InvalidSessionException {
		super.validate();
	}

	@Override
	public Serializable getId() {
		return super.getId();
	}

	@Override
	public void setId(Serializable id) {
		super.setId(id);
		isChanged = true;
	}

	@Override
	public void setStopTimestamp(Date stopTimestamp) {
		super.setStopTimestamp(stopTimestamp);
		isChanged = true;
	}

	@Override
	public void setExpired(boolean expired) {
		super.setExpired(expired);
		isChanged = true;
	}

	@Override
	public void setTimeout(long time) throws InvalidSessionException {
		super.setTimeout(time);
		isChanged = true;
	}

	@Override
	public void setHost(String host) {
		super.setHost(host);
		isChanged = true;
	}

	@Override
	public void setAttribute(Object key, Object value) throws InvalidSessionException {
		super.setAttribute(key, value);
		isChanged = true;
	}

	@Override
	public void setAttributes(Map<Object, Object> attributes) {
		super.setAttributes(attributes);
		isChanged = true;
	}

	@Override
	public Object removeAttribute(Object key) throws InvalidSessionException {
		isChanged = true;
		return super.removeAttribute(key);
	}

	@Override
	protected void expire() {
		this.stop();
		this.setExpired(true);
	}

	@Override
	public boolean equals(Object object) {
		return super.equals(object);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public Date getStartTimestamp() {
		return super.getStartTimestamp();
	}

	@Override
	public Date getLastAccessTime() {
		return super.getLastAccessTime();
	}

	@Override
	public long getTimeout() throws InvalidSessionException {
		return super.getTimeout();
	}

	@Override
	public String getHost() {
		return super.getHost();
	}

	@Override
	public void touch() throws InvalidSessionException {
		super.touch();
	}

	@Override
	public void stop() throws InvalidSessionException {
		super.stop();
	}

	@Override
	public Collection<Object> getAttributeKeys() throws InvalidSessionException {
		return super.getAttributeKeys();
	}

	@Override
	public Object getAttribute(Object key) throws InvalidSessionException {
		Object value = super.getAttribute(key);
		return value;
	}

	public Boolean getIsChanged() {
		return isChanged;
	}

	public void setIsChanged(Boolean changed) {
		isChanged = changed;
	}

	public Date getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
