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

public class CommonUtils {

	public static int countOccurrences(String subject, String matched) {
		int origialLength = subject.length();
		subject = subject.replace(matched, "");
		int newLength = subject.length();
		int count = (origialLength - newLength) / matched.length();
		return count;
	}

	public static String addZeroForNum(Integer num) {
		return addZeroForNum(num, 10);
	}

	public static String addZeroForNum(Long num) {
		return addZeroForNum(num, 19);
	}

	public static String addZeroForNum(Integer num, int length) {
		int strLen;
		String str;
		if (num == null) {
			strLen = 4;
			str = "null";
		} else {
			str = num.toString();
			strLen = str.length();
		}
		StringBuilder sb = new StringBuilder();
		while (strLen < length) {
			sb.append('0');
			strLen++;
		}
		return sb.append(str).toString();
	}

	public static String addZeroForNum(Long num, int length) {
		int strLen;
		String str;
		if (num == null) {
			strLen = 4;
			str = "null";
		} else {
			str = num.toString();
			strLen = str.length();
		}
		StringBuilder sb = new StringBuilder();
		while (strLen < length) {
			sb.append('0');
			strLen++;
		}
		return sb.append(str).toString();
	}
}
