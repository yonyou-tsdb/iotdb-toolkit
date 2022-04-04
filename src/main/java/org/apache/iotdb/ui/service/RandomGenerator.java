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

import java.util.Random;

public class RandomGenerator {

	private static String range = "0123456789abcdefghijklmnopqrstuvwxyz";

	private static final int defaultSize = 8;

	public static synchronized String getRandomString() {

		Random random = new Random();

		StringBuffer result = new StringBuffer();

		for (int i = 0; i < defaultSize; i++) {
			result.append(range.charAt(random.nextInt(range.length())));
		}

		return result.toString();
	}

	public static synchronized String getRandomString(int size) {

		Random random = new Random();

		StringBuffer result = new StringBuffer();

		for (int i = 0; i < size; i++) {
			result.append(range.charAt(random.nextInt(range.length())));
		}

		return result.toString();
	}

}
