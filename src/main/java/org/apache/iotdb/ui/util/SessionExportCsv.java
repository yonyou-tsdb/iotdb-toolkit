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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.RpcUtils;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.session.SessionDataSet.DataIterator;
import org.apache.iotdb.ui.config.websocket.WebsocketConfiguration;
import org.apache.iotdb.ui.config.websocket.WebsocketEndPoint;
import org.apache.iotdb.ui.config.websocket.WsMessageModel;
import org.apache.iotdb.ui.model.CompressMode;
import org.apache.iotdb.ui.model.ExportTimeFormat;
import org.apache.iotdb.ui.model.JobType;
import org.apache.iotdb.ui.model.WsMessage;
import org.apache.thrift.annotation.Nullable;

public class SessionExportCsv {

	private static Boolean needDataTypePrinted = true;

	public static final int EXPORT_FEEDBACK_BATCH_SIZE = 1_000_000;

	private static PipedInputStream buildCompressInput(SessionDataSet sessionDataSet, ExportTimeFormat timeFormat,
			ZoneId zoneId, String sessionId) throws IOException {
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream((PipedInputStream) input);
		new Thread(new Runnable() {
			public void run() {
				try {
					writeCsvFile(null, out, sessionDataSet, timeFormat, zoneId, sessionId);
				} catch (Exception e) {
				}
			}
		}).start();
		return input;
	}

	public static void dumpResult(String sql, int index, Session session, OutputStream outputStream,
			ExportTimeFormat timeFormat, String timeZone, CompressMode cm, String sessionId) throws Exception {
		if (timeZone != null) {
			session.setTimeZone(timeZone);
		}
		ZoneId zoneId = ZoneId.of(session.getTimeZone());
		try {
			SessionDataSet sessionDataSet = session.executeQueryStatement(sql);
			switch (cm) {
			case GZIP:
				PipedInputStream input = buildCompressInput(sessionDataSet, timeFormat, zoneId, sessionId);
				CompressUtil.GzipCompress(input, outputStream);
				break;
			case SNAPPY:
				PipedInputStream input2 = buildCompressInput(sessionDataSet, timeFormat, zoneId, sessionId);
				CompressUtil.SnappyCompress(input2, outputStream);
				break;
			default:
				writeCsvFile(null, outputStream, sessionDataSet, timeFormat, zoneId, sessionId);
				break;
			}
		} catch (StatementExecutionException | IoTDBConnectionException e) {
			throw e;
		}
	}

	public static String timeTrans(Long time, Session session, ExportTimeFormat timeFormat, String timeZone)
			throws IoTDBConnectionException {
		String timestampPrecision = "ms";
		switch (timeFormat) {
		case DEFAULT:
			String t1 = RpcUtils.parseLongToDateWithPrecision(DateTimeFormatter.ISO_OFFSET_DATE_TIME, time,
					ZoneId.of(timeZone), timestampPrecision);
			return t1;
		case NUMBER:
			String t2 = String.valueOf(time);
			return t2;
		default:
			String t3 = null;
			try {
				t3 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of(timeZone))
						.format(DateTimeFormatter.ofPattern(timeFormat.getValue()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return t3;
		}
	}

	private static Boolean writeCsvFile(@Nullable List<String> headerNames, OutputStream out,
			SessionDataSet sessionDataSet, ExportTimeFormat timeFormat, ZoneId zoneId, String sessionId)
			throws Exception {
		WebsocketEndPoint wep = WebsocketConfiguration.getWebSocketIdMap().get(sessionId);
		Long jobId = null;
		if (wep != null && wep.isOpen()) {
			jobId = wep.addJob(JobType.EXPORT);
			wep.sendMessage(
					new WsMessageModel(WsMessage.EXPORT_START, "Export job " + jobId + " begin, please wait", jobId));
		}
		List<String> columnNameList = sessionDataSet.getColumnNames();
		List<String> columnTypeList = sessionDataSet.getColumnTypes();

		DataIterator it = sessionDataSet.iterator();
		CSVPrinter printer = CSVFormat.DEFAULT.print(new OutputStreamWriter(out, "GBK"));
		if (headerNames != null) {
			printer.printRecord(headerNames);
			printer.println();
		}

		for (int i = 0; i < columnNameList.size(); i++) {
			String name = columnNameList.get(i);
			if (!needDataTypePrinted || "Time".equals(name) || "Device".equals(name)) {
				printer.print(name);
			} else {
				String type = columnTypeList.get(i);
				printer.print(new StringBuilder(name).append("(").append(type).append(")").toString());
			}
		}
		printer.println();
		long finishRowCount = 0;
		long finishPointCount = 0;
		while (it.next()) {

			if (wep != null && wep.isOpen() && jobId != null && !wep.getJobMap().containsKey(jobId)) {
				wep.sendMessage(new WsMessageModel(WsMessage.IMPORT_INTERRUPT,
						"Export job " + jobId + " be interrupted, export rows: " + finishRowCount, jobId));
				break;
			}

			finishRowCount++;
			int length = columnNameList.size();
			for (int i = 0; i < length; i++) {
				String s = columnNameList.get(i);
				Object o = it.getObject(s);
				if (i == 0 && o != null) {
					try {
						Long ol = Long.valueOf(o.toString());
						String t = timeTrans(ol, timeFormat, zoneId);
						printer.print(t);
					} catch (Exception e) {
						printer.print(null);
					}
				} else {
					printer.print(o);
				}
			}
			printer.println();
			finishPointCount += length;
			if (finishPointCount / EXPORT_FEEDBACK_BATCH_SIZE > 1) {
				finishPointCount = finishPointCount % EXPORT_FEEDBACK_BATCH_SIZE;
				if (wep != null && wep.isOpen() && jobId != null) {
					wep.sendMessage(new WsMessageModel(WsMessage.EXPORT_ONGOING,
							"Export job " + jobId + " export rows: " + finishRowCount, jobId));
				}
			}
		}
		printer.flush();
		printer.close();
		if (wep != null && wep.isOpen() && jobId != null) {
			wep.sendMessage(new WsMessageModel(WsMessage.EXPORT_FINISH,
					"Export job " + jobId + " finish, export rows: " + finishRowCount, jobId));
		}
		return true;
	}

	public static String timeTrans(Long time, ExportTimeFormat timeFormat, ZoneId zoneId) {
		String timestampPrecision = "ms";
		switch (timeFormat.getValue()) {
		case "default":
			return RpcUtils.parseLongToDateWithPrecision(DateTimeFormatter.ISO_OFFSET_DATE_TIME, time, zoneId,
					timestampPrecision);
		case "timestamp":
		case "long":
		case "number":
			return String.valueOf(time);
		default:
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), zoneId)
					.format(DateTimeFormatter.ofPattern(timeFormat.getValue()));
		}
	}

}
