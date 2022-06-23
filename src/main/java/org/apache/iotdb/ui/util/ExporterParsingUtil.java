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

import org.apache.iotdb.ui.model.exporter.ExporterBody;
import org.apache.iotdb.ui.model.exporter.ExporterHeader;
import org.apache.iotdb.ui.model.exporter.ExporterInsert;
import org.apache.iotdb.ui.model.exporter.ExporterMessageType;

public class ExporterParsingUtil {

	public static final int METRIC_COUNT_LIMIT = 100;

	public static final String COMMENT_SIGN = "#";

	public static final char LEFT_BRACE = '{';

	public static final char RIGHT_BRACE = '}';

	public static final char BLANK = ' ';

	public static final String TYPE = "TYPE";

	public static final String HELP = "HELP";

	public static final String LABEL_LEFT = "=\"";

	public static final String LABEL_RIGHT = "\",";

	public static ExporterHeader read(String metric, List<ExporterInsert> exporterInsertList, String metricName,
			ExporterMessageType type) {
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

	public static ExporterBody readBody(String metric, ExporterMessageType lastMetricType) {
		String metricName = null;
		if (metric.indexOf(LEFT_BRACE) > -1) {
			metricName = metric.substring(0, metric.indexOf(LEFT_BRACE)).trim();
		} else {
			metricName = metric.substring(0, metric.indexOf(BLANK)).trim();
		}
		if (ExporterMessageType.HISTOGRAM.equals(lastMetricType) && metricName.endsWith("_bucket")) {
			return null;
		}
		ExporterBody eb = new ExporterBody();
		if (metric.indexOf(LEFT_BRACE) > -1 && metric.indexOf(RIGHT_BRACE) > metric.indexOf(LEFT_BRACE)) {
			String label = metric.substring(metric.indexOf(LEFT_BRACE) + 1, metric.indexOf(RIGHT_BRACE));
			while (label.indexOf(LABEL_LEFT) > -1 && label.indexOf(LABEL_RIGHT) > label.indexOf(LABEL_LEFT)) {
				String labelName = label.substring(0, label.indexOf(LABEL_LEFT));
				String labelValue = label.substring(label.indexOf(LABEL_LEFT) + 2, label.indexOf(LABEL_RIGHT));
				label = label.substring(label.indexOf(LABEL_RIGHT) + 2, label.length());
				eb.getLabel().put(labelName, labelValue);
			}
		}
		eb.setMetricName(metricName);
		String metricValue = metric.substring(metric.lastIndexOf(BLANK) + 1, metric.length());
		try {
			eb.setValue(Double.valueOf(metricValue));
		} catch (Exception e) {
			eb.setValue(Double.NaN);
		}
		return eb;
	}
}
