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

import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.ui.model.BaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yonyou.iotdb.utils.core.ExportStarter;
import com.yonyou.iotdb.utils.core.ImportStarter;
import com.yonyou.iotdb.utils.core.pipeline.context.model.CompressEnum;
import com.yonyou.iotdb.utils.core.pipeline.context.model.ExportModel;
import com.yonyou.iotdb.utils.core.pipeline.context.model.FileSinkStrategyEnum;
import com.yonyou.iotdb.utils.core.pipeline.context.model.ImportModel;

import io.swagger.annotations.Api;
import reactor.core.Disposable;

@CrossOrigin
@RestController
@Api(value = "Util API")
public class UtilController {

	@Autowired
	private ExportStarter exportStarter;

	@Autowired
	private ImportStarter importStarter;

	@RequestMapping(value = "/api/util/export/start", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exportStart(HttpServletRequest request) throws SQLException {
		ExportModel exportModel = new ExportModel();
		exportModel.setCharSet("utf8");
		exportModel.setCompressEnum(CompressEnum.CSV);
		exportModel.setFileFolder("E:\\export_ln4");
		exportModel.setFileSinkStrategyEnum(FileSinkStrategyEnum.EXTRA_CATALOG);
		exportModel.setIotdbPath("root._monitor.\"115.28.134.232\".\"jvm_memory_committed_bytes\"");
//		exportModel.setIotdbPath("root.ln");
		exportModel.setNeedTimeseriesStructure(true);
		Session session = new Session("172.20.45.128", "6667", "root", "root");
		try {
			session.open();
		} catch (IoTDBConnectionException e) {
			e.printStackTrace();
		}
		exportModel.setSession(session);
		Disposable d = exportStarter.start(exportModel);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(d.isDisposed());
		return null;
	}

	@RequestMapping(value = "/api/util/import/start", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> importStart(HttpServletRequest request) throws SQLException {
		ImportModel importModel = new ImportModel();
		importModel.setCharSet("utf8");
		importModel.setCompressEnum(CompressEnum.CSV);
		importModel.setNeedTimeseriesStructure(false);
		Session session = new Session("172.20.45.128", "6667", "root", "root");
		try {
			session.open();
		} catch (IoTDBConnectionException e) {
			e.printStackTrace();
		}
		importModel.setSession(session);
		importModel.setFileFolder("E:\\export_ln4");
		importStarter.start(importModel);
		return null;
	}

	@RequestMapping(value = "/api/util/export/progress", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> exportProgress(HttpServletRequest request) throws SQLException {
		Double[] d = exportStarter.rateOfProcess();
		return BaseVO.success(d);
	}

	@RequestMapping(value = "/api/util/import/progress", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseVO<Object> importProgress(HttpServletRequest request) throws SQLException {
		Double[] d = importStarter.rateOfProcess();
		return BaseVO.success(d);
	}
}
