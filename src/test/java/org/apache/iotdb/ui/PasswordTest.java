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
package org.apache.iotdb.ui;

import org.apache.iotdb.ui.controller.IotDBController;
import org.apache.iotdb.ui.util.CommonUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class, databaseConnection = { "dataSource1" })
public class PasswordTest {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Test
	public void test1() {
		String encodedPassword = new SimpleHash("md5", "root", "", 2).toString();
		Assert.assertEquals("bc73dbfcdf2e5ef99e99407fc8ae8de3", encodedPassword);
		String encoded = bCryptPasswordEncoder.encode("123456");
		System.out.println("pass:" + encoded);
		Assert.assertTrue(bCryptPasswordEncoder.matches("123456", encoded));
	}

	@Test
	public void testSql() {
		String reg = IotDBController.REG;
		Assert.assertFalse("list user;".matches(reg));
		Assert.assertTrue("list user".matches(reg));
		Assert.assertFalse(";list user".matches(reg));
		Assert.assertFalse("list;user".matches(reg));
	}

	@Test
	public void testCountOccurrences() {
		Assert.assertEquals(2, CommonUtils.countOccurrences("root.demo.beijing : DELETE_TIMESERIES", "."));
		Assert.assertEquals(0, CommonUtils.countOccurrences("root_demo_beijing : DELETE_TIMESERIES", "."));
		Assert.assertEquals(3, CommonUtils.countOccurrences("root.demo..beijing : DELETE_TIMESERIES", "."));
		Assert.assertEquals(1, CommonUtils.countOccurrences("root.demo..beijing : DELETE_TIMESERIES", ".."));
	}
}
