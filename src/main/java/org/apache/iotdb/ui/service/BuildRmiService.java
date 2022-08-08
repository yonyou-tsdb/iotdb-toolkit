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
package org.apache.iotdb.ui.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.iotdb.ui.config.MonitorRmiConfig;
import org.apache.iotdb.ui.util.CompilerUtils;
import org.apache.iotdb.ui.util.CreateJarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildRmiService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BuildRmiService.class);

	@Autowired
	private MonitorRmiConfig monitorRmiConfig;

//	String basePath = new StringBuilder("E:").append(File.separator).append("workspace1").append(File.separator).append("DynamicTrigger").toString();
//	String basePath = "E:\\workspace1\\world";
	// 需要编译的源文件路径
//	String[] srcFiles = {
//			new StringBuilder(File.separator).append("src").append(File.separator).append("cn").append(File.separator)
//					.append("poet").append(File.separator).append("bean").append(File.separator).toString(),
//			File.separator + "src" + File.separator + "poem" + File.separator + "cn" + File.separator + "poet"
//					+ File.separator };
//	String[] srcFiles = { "/src/world/", };
	// 依赖包所在路径
//	String jarReyOnPath = "E:\\workspace1\\DynamicTrigger\\lib";
//	String jarReyOnPath = "E:\\workspace1\\world\\lib";
	// 生成jar文件路径
//	String jarFilePath = "E:\\workspace1\\apache-iotdb-0.12.1-github\\iotdb\\server\\target\\iotdb-server-0.14.0-SNAPSHOT\\ext\\trigger";
//  String jarFilePath = "E:\\workspace1\\world";
	// 生成jar文件名称
//	String jarFileName = "DynamicTrigger";
//	String jarFileName = "world";
	String encoding = "utf-8";

	public void buildRmi() {
		String sourcePath = "";
		String classPath = "";
		try {
			// 将RMI需要使用的JAVA文件拷贝到制定目录中
			String source = UUID.randomUUID().toString().replaceAll("-", "");
			System.out.println("分隔符:" + File.separator);
			System.out.println("资源拷贝......");
			sourcePath = monitorRmiConfig.getTempPath() + File.separator + source;
			copySource(sourcePath);// 拷贝资源
			System.out.println("资源拷贝结束");
			System.out.println("编译资源......");
			// 编译java文件
			classPath = monitorRmiConfig.getTempPath() + File.separator + source + File.separator + "class";
			try {
				CompilerUtils.compiler(sourcePath, classPath, monitorRmiConfig.getBasePath(), encoding,
						monitorRmiConfig.getJarReyOnPath());
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			System.out.println("编译资源结束");
			System.out.println("生成jar......");
			// 生成jar文件
			CreateJarUtils.createTempJar(classPath, monitorRmiConfig.getJarFilePath(), source);
			System.out.println("生成jar完成");
			// 删除临时文件
			ExeSuccess(sourcePath, classPath);
		} catch (IOException e) {
			LOGGER.error("", e);
			deleteTempFile(sourcePath, classPath);

		} finally {
		}
	}

	private void ExeSuccess(String sourcePath, String classPath) {
		final String sourcedir = sourcePath;
		final String classdir = classPath;
		// 程序结束后，通过以下代码删除生成的文件
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				deleteTempFile(sourcedir, classdir);
				System.out.println("***************执行完毕**********************");
			}
		});
	}

	private void deleteTempFile(String sourcePath, String classPath) {
		// 程序结束后，通过以下代码删除生成的class 和java文件
		try {
			File sourceFile = new File(sourcePath);
			if (sourceFile.exists()) {
				FileUtils.deleteDirectory(sourceFile);
			}
			File classFile = new File(classPath);
			if (classFile.exists()) {
				FileUtils.deleteDirectory(classFile);
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}

	}

	private void copySource(String sourcePath) throws IOException {
		for (String f : monitorRmiConfig.getSrcFiles()) {
			String path = f.replace("/", File.separator);
			System.out.println(path);
			File srcFile = new File(monitorRmiConfig.getBasePath() + path);
			File targetFile = new File(sourcePath + path);
			FileUtils.copyDirectory(srcFile, targetFile, new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					System.out.println(pathname);
					return pathname.getName().endsWith(".java");
				}
			});
		}
	}
}
