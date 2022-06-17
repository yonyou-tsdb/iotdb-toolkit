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
package org.apache.iotdb.ui.util;

import java.util.List;

import org.apache.iotdb.ui.model.ExporterHeader;
import org.apache.iotdb.ui.model.ExporterInsert;
import org.apache.iotdb.ui.model.ExporterMessageType;

public class ExporterParsingUtil {

	public static final int METRIC_COUNT_LIMIT = 100;

	public static final String COMMENT_SIGN = "#";

	public static final String TYPE = "TYPE";

	public static final String HELP = "HELP";

	public static ExporterHeader read(String metric, List<ExporterInsert> exporterInsertList, String metricName,
			ExporterMessageType type) {
		if (metric.startsWith(COMMENT_SIGN)) {
			ExporterHeader eh = new ExporterHeader();
			String temp = metric.substring(1).trim();
			if (temp.startsWith(TYPE)) {
				temp = temp.substring(4).trim();
				eh.setMetricName(temp.substring(0, temp.indexOf(' ')));
				eh.setType(ExporterMessageType.forValue(temp.substring(temp.lastIndexOf(' '), temp.length()).trim()));
			} else if (temp.startsWith(HELP)) {
				temp = temp.substring(4).trim();
				eh.setMetricName(temp.substring(0, temp.indexOf(' ') > -1 ? temp.indexOf(' ') : temp.length()));
			}
			return eh;
		}

		return null;
	}

}
