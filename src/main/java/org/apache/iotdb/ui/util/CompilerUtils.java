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
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.StringUtils;

public class CompilerUtils {
	private static JavaCompiler javaCompiler;
	private static String encoding = "UTF-8";

	private CompilerUtils() {
	};

	private static JavaCompiler getJavaCompiler() {
		if (javaCompiler == null) {
			synchronized (CompilerUtils.class) {
				if (javaCompiler == null) {
					// 根据JavaCompiler 的获取方式来看，应该是采用了单例模式的，但是这里为了顺便复习一下单例模式，以及确保一下单例吧 TODO 此处单例有改进空间
					javaCompiler = ToolProvider.getSystemJavaCompiler();
				}
			}
		}

		return javaCompiler;
	}

	public static boolean isWindows() {
		if ("\\".equals(File.separator)) {
			return true;
		}
		return false;
	}

	public static void compiler(String filePath, String targetDir, String sourceDir, String encoding, String jarPath)
			throws Exception {
		// 得到filePath目录下的所有java源文件
		List<File> sourceFileList = File4ComplierUtils.getSourceFiles(filePath);

		if (sourceFileList.size() == 0) {
			// 没有java文件，直接返回
			System.out.println(filePath + "目录下查找不到任何java文件");
			return;
		}
		// 获取所有jar
		String jars = File4ComplierUtils.getJarFiles(jarPath);
		if (StringUtils.isBlank(jars)) {
			jars = "";
		}
		File targetFile = new File(targetDir);
		if (!targetFile.exists())
			targetFile.mkdirs();
		// 建立DiagnosticCollector对象
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		// 该文件管理器实例的作用就是将我们需要动态编译的java源文件转换为getTask需要的编译单元
		StandardJavaFileManager fileManager = getJavaCompiler().getStandardFileManager(diagnostics, null, null);
		// 获取要编译的编译单元
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
		/**
		 * 编译选项，在编译java文件时，编译程序会自动的去寻找java文件引用的其他的java源文件或者class。
		 * -sourcepath选项就是定义java源文件的查找目录， -classpath选项就是定义class文件的查找目录，-d就是编译文件的输出目录。
		 */
		// Iterable<String> options
		// =Arrays.asList("-encoding",encoding,"-classpath",jars,"-d", targetDir,
		// "-sourcepath", sourceDir);
		Iterable<String> options = Arrays.asList("-encoding", encoding, "-classpath", jars, "-d", targetDir,
				"-sourcepath", sourceDir);
		/**
		 * 第一个参数为文件输出，这里我们可以不指定，我们采用javac命令的-d参数来指定class文件的生成目录 第二个参数为文件管理器实例
		 * fileManager 第三个参数DiagnosticCollector<JavaFileObject>
		 * diagnostics是在编译出错时，存放编译错误信息
		 * 第四个参数为编译命令选项，就是javac命令的可选项，这里我们主要使用了-d和-sourcepath这两个选项 第五个参数为类名称
		 * 第六个参数为上面提到的编译单元，就是我们需要编译的java源文件
		 */
		JavaCompiler.CompilationTask task = getJavaCompiler().getTask(null, fileManager, diagnostics, options, null,
				compilationUnits);
		// 运行编译任务
		// 编译源程式
		boolean success = task.call();
		for (Diagnostic diagnostic : diagnostics.getDiagnostics())
			System.out.printf(
					"Code: %s%n" + "Kind: %s%n" + "Position: %s%n" + "Start Position: %s%n" + "End Position: %s%n"
							+ "Source: %s%n" + "Message: %s%n",
					diagnostic.getCode(), diagnostic.getKind(), diagnostic.getPosition(), diagnostic.getStartPosition(),
					diagnostic.getEndPosition(), diagnostic.getSource(), diagnostic.getMessage(null));
		fileManager.close();
		System.out.println((success) ? "编译成功" : "编译失败");
	}
}
