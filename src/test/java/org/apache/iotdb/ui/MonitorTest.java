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
package org.apache.iotdb.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.ui.model.exporter.ExporterBody;
import org.apache.iotdb.ui.model.exporter.ExporterHeader;
import org.apache.iotdb.ui.model.exporter.ExporterInsert;
import org.apache.iotdb.ui.model.exporter.ExporterMessageType;
import org.apache.iotdb.ui.util.ExporterParsingUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class, databaseConnection = { "dataSource1" })
public class MonitorTest {

	public String uri = "http://localhost/";

//	public String uri = "http://172.20.48.111:9091/metrics";

	@Autowired
	@Qualifier("httpClientBean")
	private CloseableHttpClient HttpClientBean;

	@Test
	public void test0() {
		Assert.assertNotNull(HttpClientBean);
		try {
			readMetrics();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readMetrics() throws Exception {
		HttpGet http = new HttpGet(uri);
		Session session = new Session("172.20.48.111", 6667, "root", "root");
		session.open();
		Long timestamp = Calendar.getInstance().getTime().getTime();
		// 发送请求，获取服务器返回的httpResponse对象
		try (CloseableHttpResponse httpResponse = HttpClientBean.execute(http);
				// 用输入流获取，字节读取
				InputStream inputStream = httpResponse.getEntity().getContent();
				// 转换成字符流
				InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
				// 缓冲字符流，提供字符、数组和行的高效读取
				BufferedReader br = new BufferedReader(reader);) {
			// 行读取
			String line = null;
			ExporterMessageType lastMetricType = ExporterMessageType.UNTYPE;
			ExporterInsert ei = new ExporterInsert();
			while ((line = br.readLine()) != null) {
				if (line.startsWith(ExporterParsingUtil.COMMENT_SIGN)) {
					ExporterHeader eh = ExporterParsingUtil.read(line, null, null, null);
					lastMetricType = eh.getType();
				} else {
					ExporterBody eb = ExporterParsingUtil.readBody(line, lastMetricType);
					if (eb != null) {
						ei.addExporterBody(eb, timestamp);
					}
					if (ei.getSize() >= 100) {
						ei.batchInsert(session);
					}
				}
			}
			ei.batchInsert(session);
		}
	}
}
