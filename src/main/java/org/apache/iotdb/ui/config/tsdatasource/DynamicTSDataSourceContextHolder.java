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

public class DynamicTSDataSourceContextHolder {

	private DynamicTSDataSourceContextHolder() {

	}

	private static final ThreadLocal<Object> contextHolder = ThreadLocal.withInitial(Object::new);

	private static final ThreadLocal<Object> survivedContextHolder = ThreadLocal.withInitial(Object::new);

	public static void setSessionPoolKey(Object key) {
		contextHolder.set(key);
	}

	public static Object getSessionPoolKey() {
		return contextHolder.get();
	}

	public static void clearSessionPoolKey() {
		contextHolder.remove();
	}

	public static void survivedOldAndSetNewSessionPoolKey(Object key) {
		survivedContextHolder.set(contextHolder.get());
		contextHolder.set(key);
	}

	public static void useSurvivedReplacePresentSessionPoolKey() {
		if (survivedContextHolder.get() != null) {
			contextHolder.set(survivedContextHolder.get());
			survivedContextHolder.remove();
		}
	}
}
