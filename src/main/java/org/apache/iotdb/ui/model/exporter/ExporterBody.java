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
package org.apache.iotdb.ui.model.exporter;

import java.util.Map;
import java.util.TreeMap;

public class ExporterBody {

	private String metricName;

	private Double value;

	private Map<String, String> label = new TreeMap<>();

	private Long timestamp;

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(Map<String, String> label) {
		this.label = label;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String buildPath() {
		if (label.isEmpty()) {
			return String.format("\"%s\"", metricName);
		} else {
			StringBuilder sb = new StringBuilder("\"").append(metricName).append("\"");
			for (Map.Entry<String, String> e : label.entrySet()) {
				sb.append(".\"").append(e.getKey()).append("=").append(e.getValue()).append("\"");
			}
			return sb.toString();
		}
	}

}
