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

import static org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.BOOLEAN;
import static org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.DOUBLE;
import static org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.FLOAT;
import static org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT32;
import static org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT64;
import static org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.session.SessionDataSet.DataIterator;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.ui.config.websocket.WebsocketConfiguration;
import org.apache.iotdb.ui.config.websocket.WebsocketEndPoint;
import org.apache.iotdb.ui.config.websocket.WsMessageModel;
import org.apache.iotdb.ui.model.CompressMode;
import org.apache.iotdb.ui.model.JobType;
import org.apache.iotdb.ui.model.WsMessage;
import org.apache.thrift.annotation.Nullable;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SessionImportCsv {

	private static final Log logger = LogFactory.getLog(SessionImportCsv.class);

	private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	public static final int IMPORT_INSERT_BANCH_SIZE = 10_000;

	public static final int IMPORT_FEEDBACK_BATCH_SIZE = 100_000;

	public static void importFromTargetPath(String host, int port, String username, String password,
			InputStream inputStream, String timeZone, CompressMode compressMode, String wssessionId) throws Exception {
		Session session = null;
		try {
			session = new Session(host, Integer.valueOf(port), username, password, false);
			session.open(false);
			session.setTimeZone(timeZone);
			Stream<CSVRecord> csvRecords = null;
			switch (compressMode) {
			case GZIP:
				csvRecords = getCompressParser(inputStream, CompressMode.GZIP);
				break;
			case SNAPPY:
				csvRecords = getCompressParser(inputStream, CompressMode.SNAPPY);
				break;
			default:
				csvRecords = getParser(inputStream);
				break;
			}
			writeDataAlignedByTime(csvRecords, session, wssessionId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(new StringBuilder("Encounter an error when connecting to server, because ")
					.append(e.getMessage()).toString());
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	private static Stream<CSVRecord> getParser(InputStream inputStream) throws Exception {
		Stream<CSVRecord> csvRecords = readCsvFile(inputStream);
		return csvRecords;
	}

	private static Stream<CSVRecord> getCompressParser(InputStream inputStream, CompressMode cm) throws Exception {
		InputStream input = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream((PipedInputStream) input);
		new Thread(new Runnable() {
			public void run() {
				try {
					switch (cm) {
					case SNAPPY:
						CompressUtil.SnappyUncompress(inputStream, out);
						break;
					case GZIP:
						CompressUtil.GzipUncompress(inputStream, out);
						break;
					default:
						CompressUtil.GzipUncompress(inputStream, out);
						break;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		Stream<CSVRecord> csvRecords = readCsvFile(input);
		return csvRecords;
	}

	private static void writeDataAlignedByTime(Stream<CSVRecord> records, Session session, String wssessionId) {
		WebsocketEndPoint wep = WebsocketConfiguration.getWebSocketIdMap().get(wssessionId);
		Long jobId = null;
		if (wep != null && wep.isOpen()) {
			jobId = wep.addJob(JobType.IMPORT);
			wep.sendMessage(
					new WsMessageModel(WsMessage.IMPORT_START, "Import job " + jobId + " begin, please wait", jobId));
		}
		@SuppressWarnings("unchecked")
		List<String>[] headerNames = new LinkedList[1];

		HashMap<String, List<String>> deviceAndMeasurementNames = new HashMap<>();
		HashMap<String, TSDataType> headerTypeMap = new HashMap<>();
		HashMap<String, String> headerNameMap = new HashMap<>();

		Map<String, CsvModel> m = new HashMap<>();

		boolean[] booleanArray = { true, false }; // [0]: first or not; [1]: header contains "Device" or not;
		Long finishRowCount[] = { 0L, 0L, jobId }; // [0]: finishRowCount; [1]: finishPointCount; [2]: jobId
		try {
			records.forEach(record -> {

				if (wep != null && wep.isOpen() && finishRowCount[2] != null
						&& !wep.getJobMap().containsKey(finishRowCount[2])) {
					wep.sendMessage(new WsMessageModel(WsMessage.IMPORT_INTERRUPT,
							"Import job " + finishRowCount[2] + " be interrupted, import rows: " + finishRowCount[0],
							finishRowCount[2]));
					throw new RuntimeException("Interrupt");
				}

				if (booleanArray[0]) {
					booleanArray[0] = false;
					headerNames[0] = new LinkedList<>();
					Map<String, String> map = record.toMap();
					headerNames[0].addAll(map.keySet());

					if (headerNames[0].contains("Device")) {
						booleanArray[1] = true;
					}

					parseHeaders(headerNames[0], deviceAndMeasurementNames, headerTypeMap, headerNameMap, session);
				}
				int length = deviceAndMeasurementNames.size();
				String d = null;
				if (booleanArray[1]) {
					d = record.get("Device");
				}
				String timeStr = record.get("Time");
				for (Map.Entry<String, List<String>> e : deviceAndMeasurementNames.entrySet()) {
					String measurement = e.getKey();
					String measurementKey = booleanArray[1]
							? new StringBuilder(d).append(".").append(measurement).toString()
							: measurement;
					if (!m.containsKey(measurementKey)) {
						m.put(measurementKey, new CsvModel());
					}
					CsvModel cm = m.get(measurementKey);
					List<String> l = e.getValue();
					List<String> devices = new LinkedList<String>();
					List<TSDataType> types = new LinkedList<TSDataType>();
					List<Object> values = new LinkedList<Object>();
					Long time = 0L;
					timeStr = timeStr.replaceAll("T", " ");
					if (timeStr.indexOf(' ') == -1) {
						try {
							time = Long.parseLong(timeStr);
						} catch (Exception e1) {
						}
					} else {
						if (timeStr.lastIndexOf('+') == timeStr.length() - 6
								|| timeStr.lastIndexOf('-') == timeStr.length() - 6) {
							timeStr = timeStr.substring(0, timeStr.length() - 6);
						}
						try {
							time = fmt.withZone(DateTimeZone.forID(session.getTimeZone())).parseDateTime(timeStr)
									.toDate().getTime();
						} catch (Exception e2) {
						}
					}

					for (int i = 0; i < l.size(); i++) {
						if (!measurement.equals(e.getKey())) {
							continue;
						}
						String ee = l.get(i);
						String header = ee;
						if (!booleanArray[1]) {
							header = new StringBuilder(e.getKey()).append(".").append(ee).toString();
						}
						String raw = record.get(header);
						if (raw != null && !"".equals(raw)) {
							String device = ee;
							if (ee.indexOf('(') > -1) {
								device = ee.substring(0, ee.indexOf('('));
							}
							devices.add(device);
							TSDataType type = headerTypeMap.get(headerNameMap.get(header));
							types.add(type);
							Object value = typeTrans(raw, type);
							values.add(value);
						} else {
						}
					}
					if (!devices.isEmpty()) {
						if (booleanArray[1]) {
							cm.setDeviceId(d);
						} else {
							cm.setDeviceId(e.getKey());
						}
						addLine(cm.getTimes(), cm.getMeasurementsList(), cm.getTypesList(), cm.getValuesList(), time,
								devices, types, values);
					}
					if (cm.getTimes().size() % IMPORT_INSERT_BANCH_SIZE == 0
							&& cm.getTimes().size() / IMPORT_INSERT_BANCH_SIZE > 0) {
						logger.warn(new StringBuilder(measurementKey).append(",")
								.append(cm.getCount() + cm.getTimes().size()).toString());
						try {
							session.insertRecordsOfOneDevice(cm.getDeviceId(), cm.getTimes(), cm.getMeasurementsList(),
									cm.getTypesList(), cm.getValuesList());
							cm.setCount(cm.getCount() + IMPORT_INSERT_BANCH_SIZE);
							cm.getTimes().clear();
							cm.getMeasurementsList().clear();
							cm.getTypesList().clear();
							cm.getValuesList().clear();
						} catch (IoTDBConnectionException | StatementExecutionException e1) {
						}
					}
				}
				finishRowCount[1] += length;
				finishRowCount[0]++;
				if (finishRowCount[1] / IMPORT_FEEDBACK_BATCH_SIZE > 1) {
					finishRowCount[1] = finishRowCount[1] % IMPORT_FEEDBACK_BATCH_SIZE;
					if (wep != null && wep.isOpen() && finishRowCount[2] != null) {
						wep.sendMessage(new WsMessageModel(WsMessage.IMPORT_ONGOING,
								"Import job " + finishRowCount[2] + " import rows: " + finishRowCount[0],
								finishRowCount[2]));
					}
				}
			});
		} catch (Exception e) {
		}
		for (Map.Entry<String, CsvModel> e2 : m.entrySet()) {
			CsvModel cm = e2.getValue();
			if (cm.getTimes().size() != 0) {
				logger.warn(new StringBuilder(e2.getKey()).append(",").append(cm.getCount() + cm.getTimes().size())
						.toString());
				try {
					session.insertRecordsOfOneDevice(cm.getDeviceId(), cm.getTimes(), cm.getMeasurementsList(),
							cm.getTypesList(), cm.getValuesList());
				} catch (IoTDBConnectionException | StatementExecutionException e1) {
				}
			}
		}
		if (wep != null && wep.isOpen() && jobId != null) {
			wep.sendMessage(new WsMessageModel(WsMessage.IMPORT_FINISH,
					"Import job " + jobId + " finish, import rows: " + finishRowCount[0], jobId));
		}
	}

	private static void addLine(List<Long> times, List<List<String>> measurements, List<List<TSDataType>> datatypes,
			List<List<Object>> values, long time, List<String> s1, List<TSDataType> s1type, List<Object> value2) {

		List<String> tmpMeasurements = new ArrayList<>();
		List<TSDataType> tmpDataTypes = new ArrayList<>();
		List<Object> tmpValues = new ArrayList<>();
		for (int i = 0; i < s1.size(); i++) {
			tmpMeasurements.add(s1.get(i));
			tmpDataTypes.add(s1type.get(i));
			tmpValues.add(value2.get(i));
		}
		times.add(time);
		measurements.add(tmpMeasurements);
		datatypes.add(tmpDataTypes);
		values.add(tmpValues);
	}

	@SuppressWarnings("deprecation")
	private static Stream<CSVRecord> readCsvFile(InputStream inputStream) throws IOException {
		return CSVFormat.EXCEL.withFirstRecordAsHeader().withQuote('\'').withEscape('\\').withIgnoreEmptyLines()
				.parse(new InputStreamReader(inputStream, "GBK")).stream();
	}

	private static void parseHeaders(List<String> headerNames,
			@Nullable HashMap<String, List<String>> deviceAndMeasurementNames,
			HashMap<String, TSDataType> headerTypeMap, HashMap<String, String> headerNameMap, Session session) {
		String regex = "(?<=\\()\\S+(?=\\))";
		Pattern pattern = Pattern.compile(regex);
		Map<String, String> columnTypeMap = null;
		for (String headerName : headerNames) {
			if (headerName.equals("Time") || headerName.equals("Device"))
				continue;
			Matcher matcher = pattern.matcher(headerName);
			String type;
			if (matcher.find()) {
				type = matcher.group();
				String headerNameWithoutType = headerName.replace("(" + type + ")", "").replaceAll("\\s+", "");
				headerNameMap.put(headerName, headerNameWithoutType);
				headerTypeMap.put(headerNameWithoutType, getType(type));
			} else {
				if (columnTypeMap == null) {
					columnTypeMap = buildColumnTypeMap(session);
				}
				headerNameMap.put(headerName, headerName);
				headerTypeMap.put(headerName, getType(columnTypeMap.get(headerName)));
			}
			String[] split = headerName.split("\\.");
			String measurementName = split[split.length - 1];
			String deviceName = headerName.replace("." + measurementName, "");
			if (deviceAndMeasurementNames != null) {
				if (!deviceAndMeasurementNames.containsKey(deviceName)) {
					deviceAndMeasurementNames.put(deviceName, new ArrayList<>());
				}
				deviceAndMeasurementNames.get(deviceName).add(measurementName);
			}
		}
	}

	private static Map<String, String> buildColumnTypeMap(Session session) {
		Map<String, String> columnTypeMap = new HashMap<>();
		try {
			SessionDataSet sessionDataSet = session.executeQueryStatement("show timeseries");
			DataIterator it = sessionDataSet.iterator();
			while (it.next()) {
				columnTypeMap.put(it.getString("timeseries"), it.getString("dataType"));
			}
		} catch (StatementExecutionException | IoTDBConnectionException e) {
			e.printStackTrace();
		}
		return columnTypeMap;
	}

	private static TSDataType getType(String typeStr) {
		if (typeStr == null) {
			return TEXT;
		}
		switch (typeStr) {
		case "TEXT":
			return TEXT;
		case "BOOLEAN":
			return BOOLEAN;
		case "INT32":
			return INT32;
		case "INT64":
			return INT64;
		case "FLOAT":
			return FLOAT;
		case "DOUBLE":
			return DOUBLE;
		default:
			return TEXT;
		}
	}

	private static Object typeTrans(String value, TSDataType type) {
		try {
			switch (type) {
			case TEXT:
				if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
					value = value.substring(1, value.length() - 1);
					value = value.replaceAll("\"\"", "\"");
				}
				return value;
			case BOOLEAN:
				if (!value.equals("true") && !value.equals("false")) {
					return null;
				}
				return Boolean.valueOf(value);
			case INT32:
				return Integer.valueOf(value);
			case INT64:
				return Long.valueOf(value);
			case FLOAT:
				return Float.valueOf(value);
			case DOUBLE:
				return Double.valueOf(value);
			default:
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static final String[] STRING_TIME_FORMAT = new String[] { "yyyy-MM-dd HH:mm:ss.SSSX",
			"yyyy/MM/dd HH:mm:ss.SSSX", "yyyy.MM.dd HH:mm:ss.SSSX", "yyyy-MM-dd HH:mm:ssX", "yyyy/MM/dd HH:mm:ssX",
			"yyyy.MM.dd HH:mm:ssX", "yyyy-MM-dd HH:mm:ss.SSSz", "yyyy/MM/dd HH:mm:ss.SSSz", "yyyy.MM.dd HH:mm:ss.SSSz",
			"yyyy-MM-dd HH:mm:ssz", "yyyy/MM/dd HH:mm:ssz", "yyyy.MM.dd HH:mm:ssz", "yyyy-MM-dd HH:mm:ss.SSS",
			"yyyy/MM/dd HH:mm:ss.SSS", "yyyy.MM.dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
			"yyyy.MM.dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy/MM/dd'T'HH:mm:ss.SSSX",
			"yyyy.MM.dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy/MM/dd'T'HH:mm:ssX", "yyyy.MM.dd'T'HH:mm:ssX",
			"yyyy-MM-dd'T'HH:mm:ss.SSSz", "yyyy/MM/dd'T'HH:mm:ss.SSSz", "yyyy.MM.dd'T'HH:mm:ss.SSSz",
			"yyyy-MM-dd'T'HH:mm:ssz", "yyyy/MM/dd'T'HH:mm:ssz", "yyyy.MM.dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss.SSS",
			"yyyy/MM/dd'T'HH:mm:ss.SSS", "yyyy.MM.dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy/MM/dd'T'HH:mm:ss",
			"yyyy.MM.dd'T'HH:mm:ss" };
}
