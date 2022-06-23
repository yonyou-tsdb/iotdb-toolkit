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

import java.util.LinkedList;
import java.util.List;

import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

public class ExporterInsert {

	private String path = "root.__metric.\"127.0.0.1:6667\"";

	private List<List<String>> measurementsList = new LinkedList<>();

	private List<List<TSDataType>> typesList = new LinkedList<>();

	private List<Long> timestamps = new LinkedList<>();

	private List<String> deviceIds = new LinkedList<>();

	private int size;

	private List<List<Object>> valuesList = new LinkedList<>();;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void addExporterBody(ExporterBody exporterBody, Long timestamp) {
		timestamps.add(timestamp);
		List<Object> values = new LinkedList<>();
		valuesList.add(values);
		List<String> measurements = new LinkedList<>();
		measurementsList.add(measurements);
		List<TSDataType> types = new LinkedList<>();
		typesList.add(types);
//		deviceIds.add(path + ".\"" + exporterBody.getMetricName() + exporterBody.getLabel().toString().replace(" ", "_")
//				+ "\"");
		deviceIds.add(path + "." + exporterBody.buildPath());
		measurements.add("value");
		types.add(TSDataType.DOUBLE);
		values.add(exporterBody.getValue());
		size++;
	}

	public void clear() {
		deviceIds.clear();
		timestamps.clear();
		measurementsList.clear();
		typesList.clear();
		valuesList.clear();
		size = 0;
	}

	public void batchInsert(Session session, Long timestamp) {
		System.out.println("  ++ " + deviceIds.size() + "," + timestamps.size() + "," + measurementsList.size() + ","
				+ valuesList.size());
//		timestamps.add(timestamp);
		try {
			session.insertRecords(deviceIds, timestamps, measurementsList, typesList, valuesList);
//			RecordsOfOneDevice(path, timestamps, measurementsList, typesList, valuesList);
		} catch (IoTDBConnectionException | StatementExecutionException e1) {
			e1.printStackTrace();
		}
		clear();
	}

	public List<List<Object>> getValuesList() {
		return valuesList;
	}

	public void setValuesList(List<List<Object>> valuesList) {
		this.valuesList = valuesList;
	}

	public List<List<String>> getMeasurementsList() {
		return measurementsList;
	}

	public void setMeasurementsList(List<List<String>> measurementsList) {
		this.measurementsList = measurementsList;
	}

	public List<List<TSDataType>> getTypesList() {
		return typesList;
	}

	public void setTypesList(List<List<TSDataType>> typesList) {
		this.typesList = typesList;
	}

}
