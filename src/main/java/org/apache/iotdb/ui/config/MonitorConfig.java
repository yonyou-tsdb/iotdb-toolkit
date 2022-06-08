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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorConfig {

	@Value("${iotdbui.monitor.rmi.basePath:}")
	private String rmiBasePath;

	@Value("${iotdbui.monitor.rmi.srcFiles:}")
	private String[] rmiSrcFiles;

	@Value("${iotdbui.monitor.rmi.jarReyOnPath:}")
	private String rmiJarReyOnPath;

	@Value("${iotdbui.monitor.rmi.jarFilePath:}")
	private String rmiJarFilePath;

	public String getRmiBasePath() {
		return rmiBasePath;
	}

	public void setRmiBasePath(String rmiBasePath) {
		this.rmiBasePath = rmiBasePath;
	}

	public String[] getRmiSrcFiles() {
		return rmiSrcFiles;
	}

	public void setRmiSrcFiles(String[] rmiSrcFiles) {
		this.rmiSrcFiles = rmiSrcFiles;
	}

	public String getRmiJarReyOnPath() {
		return rmiJarReyOnPath;
	}

	public void setRmiJarReyOnPath(String rmiJarReyOnPath) {
		this.rmiJarReyOnPath = rmiJarReyOnPath;
	}

	public String getRmiJarFilePath() {
		return rmiJarFilePath;
	}

	public void setRmiJarFilePath(String rmiJarFilePath) {
		this.rmiJarFilePath = rmiJarFilePath;
	}

}
