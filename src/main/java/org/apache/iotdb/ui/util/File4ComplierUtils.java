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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class File4ComplierUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(File4ComplierUtils.class);

	public static List<File> getSourceFiles(String sourceFilePath) {

		List<File> sourceFileList = new ArrayList<>();
		try {
			getSourceFiles(new File(sourceFilePath), sourceFileList);
		} catch (Exception e) {
			LOGGER.error("", e);
			sourceFileList = null;
		}
		return sourceFileList;
	}

	public static String getJarFiles(String sourceFilePath) {

		String jars = "";
		try {
			jars = getJarFiles(new File(sourceFilePath), jars);
		} catch (Exception e) {
			LOGGER.error("", e);
			jars = "";
		}
		return jars;
	}

	private static void getSourceFiles(File sourceFile, List<File> sourceFileList) throws Exception {
		if (!sourceFile.exists()) {
			// 文件或者目录必须存在
			throw new IOException(String.format("%s目录不存在", sourceFile.getPath()));
		}
		if (null == sourceFileList) {
			// 若file对象为目录
			throw new NullPointerException("参数异常");
		}
		if (sourceFile.isDirectory()) {// 若file对象为目录
			File[] childrenDirectoryFiles = sourceFile.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			for (File file : sourceFile.listFiles()) {
				if (file.isDirectory()) {
					getSourceFiles(file, sourceFileList);
				} else {
					sourceFileList.add(file);
				}
			}
		} else {
			sourceFileList.add(sourceFile);
		}
	}

	private static String getJarFiles(File sourceFile, String jars) throws Exception {
		String delimiter = CompilerUtils.isWindows() ? ";" : ":";
		if (!sourceFile.exists()) {
			// 文件或者目录必须存在
			throw new IOException("jar目录不存在");
		}
		if (!sourceFile.isDirectory()) {
			// 若file对象为目录
			throw new IOException("jar路径不为目录");
		}
		if (sourceFile.isDirectory()) {
			for (File file : sourceFile.listFiles()) {
				if (file.isDirectory()) {
					getJarFiles(file, jars);
				} else {
					jars = new StringBuilder(jars).append(file.getPath()).append(delimiter).toString();
				}
			}
		} else {
			jars = new StringBuilder(jars).append(sourceFile.getPath()).append(delimiter).toString();
		}
		return jars;
	}
}
