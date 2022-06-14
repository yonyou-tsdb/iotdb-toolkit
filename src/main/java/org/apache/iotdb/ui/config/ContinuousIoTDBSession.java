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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.ui.config.tsdatasource.SessionDataSetWrapper;
import org.apache.iotdb.ui.config.websocket.TimerConfig;

public class ContinuousIoTDBSession {

	public static Map<String, SessionDataSetWrapper> continuousDataSetWrapperMap = new ConcurrentHashMap<>();

	public static SessionDataSet getContinuousDataSet(String key) {
		SessionDataSetWrapper ret = continuousDataSetWrapperMap.get(key);
		return ret == null ? null : ret.getSessionDataSet();
	}

	public static SessionDataSetWrapper getContinuousDataSetWrapper(String key) {
		return continuousDataSetWrapperMap.get(key);
	}

	public static SessionDataSet addContinuousDataSet(String key, SessionDataSet sessionDataSet) {
		SessionDataSetWrapper sdsw = new SessionDataSetWrapper(TimerConfig.cou, sessionDataSet);
		SessionDataSetWrapper ret = continuousDataSetWrapperMap.put(key, sdsw);
		return ret == null ? null : ret.getSessionDataSet();
	}

	public static SessionDataSet removeContinuousDataSet(String key) {
		SessionDataSetWrapper ret = continuousDataSetWrapperMap.remove(key);
		return ret == null ? null : ret.getSessionDataSet();
	}
}
