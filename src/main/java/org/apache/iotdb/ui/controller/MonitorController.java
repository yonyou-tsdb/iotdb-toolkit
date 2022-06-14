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
package org.apache.iotdb.ui.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.iotdb.ui.model.BaseVO;
import org.apache.iotdb.ui.service.BuildRmiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@CrossOrigin
@RestController
@Api(value = "Monitor API")
public class MonitorController {
	
	@Autowired
	private BuildRmiService buildRmiService;
	
	@Autowired
	@Qualifier("httpClientBean")
	private CloseableHttpClient HttpClientBean;
	
	@RequestMapping(value = "/api/monitor/buildRmi", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> buildRmi(HttpServletRequest request) throws SQLException {
		buildRmiService.buildRmi();
		return null;
	}

	@RequestMapping(value = "/api/monitor/readExporter", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> readExporter(HttpServletRequest request) throws SQLException {
//		String resStr = RequestUtil.getHttpResponse(apiPath);
//		CloseableHttpClient httpclient2 = ApplicationContextProvider.getBean("httpClientBean",
//				CloseableHttpClient.class);
		return null;
	}

}
