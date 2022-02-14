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
package org.apache.iotdb.ui.model;

public enum ExportTimeFormat {

	DEFAULT("default"), NUMBER("number"), yyyyMMddTHHmmss("yyyy-MM-dd HH:mm:ss"),
	yyyyMMddTHHmmssSSS("yyyy-MM-dd HH:mm:ss.SSS");

	private final String value;

	private ExportTimeFormat(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ExportTimeFormat forValue(String value) {
		switch (value) {
		case "number":
			return NUMBER;
		case "default":
			return DEFAULT;
		case "yyyy-MM-dd HH:mm:ss":
			return yyyyMMddTHHmmss;
		case "yyyy-MM-dd HH:mm:ss.SSS":
			return yyyyMMddTHHmmssSSS;
		default:
			return DEFAULT;
		}
	}
}
