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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration("initialConfig")
public class InitialConfig {

	@Value("${dataSource1.schema}")
	private String schema;

	@Autowired
	@Qualifier("dataSource1")
	private DataSource dataSource1;

	@Bean(name = "jdbcTemplate1")
	@Primary
	public JdbcTemplate jdbcTemplate1() {
		return new JdbcTemplate(dataSource1);
	}

	@PostConstruct
	private void init1() throws IOException {
		initCustomerDataSource1();
	}

	private void initCustomerDataSource1() throws IOException {
		try {
			String s = getFileAsOneLine(schema);
			jdbcTemplate1().execute(s);
		} catch (Exception e) {
		}
	}

	public String getFileAsOneLine(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		String str = null;
		try (InputStream is = getClass().getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
			while ((str = br.readLine()) != null) {
				if (str.startsWith("--")) {
					// remove '--' comment
					str = null;
				} else {
					sb.append(str);
				}
			}
		}
		return sb.toString();
	}
}
