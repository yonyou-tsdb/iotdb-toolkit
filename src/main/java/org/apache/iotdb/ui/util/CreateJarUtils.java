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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class CreateJarUtils {

	public static void createTempJar(String rootPath, String targetPath, String jarFileName) throws IOException {
		if (!new File(rootPath).exists()) {
			throw new IOException(String.format("%s路径不存在", rootPath));
		}
		if (StringUtils.isBlank(jarFileName)) {
			throw new NullPointerException("jarFileName为空");
		}
		// 生成META-INF文件
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
		// manifest.getMainAttributes().putValue("Main-Class", "Show");//指定Main Class
		// 创建临时jar
		File jarFile = File.createTempFile("edwin-", ".jar", new File(System.getProperty("java.io.tmpdir")));
		JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile), manifest);
		createTempJarInner(out, new File(rootPath), "");
		out.flush();
		out.close();
		// 程序结束后，通过以下代码删除生成的jar文件
		/*
		 * Runtime.getRuntime().addShutdownHook(new Thread() { public void run() {
		 * jarFile.delete(); } });
		 */
		// 生成目标路径
		File targetFile = new File(targetPath);
		if (!targetFile.exists())
			targetFile.mkdirs();
		File targetJarFile = new File(targetPath + File.separator + jarFileName + ".jar");
		if (targetJarFile.exists() && targetJarFile.isFile())
			targetJarFile.delete();
		FileUtils.moveFile(jarFile, targetJarFile);
		// jarFile.renameTo(new File(""));
	}

	private static void createTempJarInner(JarOutputStream out, File f, String base) throws IOException {

		if (f.isDirectory()) {
			File[] fl = f.listFiles();
			if (base.length() > 0) {
				base = base + "/";
			}
			for (int i = 0; i < fl.length; i++) {
				createTempJarInner(out, fl[i], base + fl[i].getName());
			}
		} else {
			out.putNextEntry(new JarEntry(base));
			FileInputStream in = new FileInputStream(f);
			byte[] buffer = new byte[1024];
			int n = in.read(buffer);
			while (n != -1) {
				out.write(buffer, 0, n);
				n = in.read(buffer);
			}
			in.close();
		}
	}
}
