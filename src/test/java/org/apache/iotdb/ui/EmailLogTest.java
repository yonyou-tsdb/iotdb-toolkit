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

import java.util.List;

import javax.sql.DataSource;

import org.apache.iotdb.ui.entity.EmailLog;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.face.EmailLogFace;
import org.apache.iotdb.ui.mapper.EmailLogDao;
import org.apache.iotdb.ui.mapper.UserDao;
import org.apache.iotdb.ui.service.EmailLogService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.alibaba.fastjson.JSONObject;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class, databaseConnection = { "dataSource1" })
public class EmailLogTest {

	@Autowired
	private DataSource dataSource1;

	@Autowired
	private EmailLogDao emailLogDao;

	@Autowired
	private EmailLogService emailLogService;

	@Autowired
	private UserDao userDao;

	@Test
	public void test0() {
		Assert.assertTrue(true);
		Assert.assertNotNull(dataSource1);
		Assert.assertNotNull(emailLogDao);
	}

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/emailLogTest/test1.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/emailLogTest/test1.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/emailLogTest/test1.xml")
	public void test1() {
		EmailLog el = emailLogDao.select(111L);
		Assert.assertEquals("rp1", el.getTempPassword());
		Assert.assertEquals("name1", el.getUser().getName());

		User user = userDao.select(212L);
		EmailLog elc = new EmailLog();
		elc.setTempPassword("rp3");
		emailLogService.loadUser(user, elc);
		Object[] list = user.getEmailLog().toArray();
		Assert.assertEquals(1, list.length);
		EmailLog emailLog1 = (EmailLog) list[0];
		Assert.assertEquals(113, emailLog1.getId().longValue());

		EmailLog el3 = new EmailLog();
		el3.setId(100L);
		el3.setEmail("aa@aa.aa");
		emailLogDao.insert(el3);
		System.out.println(JSONObject.toJSONString(el3));
		emailLogDao.delete(el3);
	}
}
