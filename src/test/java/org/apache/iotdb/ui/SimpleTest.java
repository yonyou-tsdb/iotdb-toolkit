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

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.iotdb.ui.condition.QueryCondition;
import org.apache.iotdb.ui.entity.Connect;
import org.apache.iotdb.ui.entity.Query;
import org.apache.iotdb.ui.entity.User;
import org.apache.iotdb.ui.mapper.ConnectDao;
import org.apache.iotdb.ui.mapper.QueryDao;
import org.apache.iotdb.ui.mapper.UserDao;
import org.apache.iotdb.ui.service.ConnectService;
import org.apache.iotdb.ui.service.QueryService;
import org.apache.iotdb.ui.util.CommonUtils;
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
public class SimpleTest {

	@Autowired
	private DataSource dataSource1;

	@Autowired
	private ConnectDao connectDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private QueryDao queryDao;

	@Autowired
	private QueryService queryService;

	@Autowired
	private ConnectService connectService;

	@Test
	public void test0() {
		Assert.assertTrue(true);
		Assert.assertNotNull(dataSource1);
		Assert.assertNotNull(connectDao);
		Assert.assertNotNull(userDao);
		Assert.assertNotNull(queryDao);
		Assert.assertNotNull(queryService);
	}

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/simpleTest/test1.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/simpleTest/test1.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/simpleTest/test1.xml")
	public void test1() {
		Connect connect = connectDao.select(111L);
		Assert.assertEquals("username1", connect.getUsername());
		Assert.assertEquals("name1", connect.getUser().getName());
		System.out.println("::" + JSONObject.toJSONString(connect));

		List<Connect> list = connectDao.selectAll(new Connect());
		Assert.assertEquals(2, list.size());

		Connect connect2 = new Connect();
		connect2.setUsername("usernamex");
		connectDao.insert(connect2);
		System.out.println("::" + connect2.getId());

		User user = userDao.select(211L);
		Assert.assertEquals("name1", user.getName());

		connectService.loadUser(user, new Connect());
		Collection<Connect> cc = (Collection<Connect>) user.getConnect();
		Assert.assertEquals(1, cc.size());

		List<User> list2 = userDao.selectAll(new User());
		Assert.assertEquals(2, list2.size());

		User user2 = new User();
		user2.setName("namex");
		userDao.insert(user2);

		Query query = queryDao.select(311L);
		Assert.assertEquals("sql1", query.getSqls());
		Assert.assertEquals("username1", query.getConnect().getUsername());

		List<Query> list3 = queryDao.selectAll(new Query());
		Assert.assertEquals(2, list3.size());

		Query query2 = new Query();
		query2.setSqls("sqlx");
		queryDao.insert(query2);

		queryService.loadConnect(connect, new Query());
		Collection<Query> qc = (Collection<Query>) connect.getQuery();
		Assert.assertEquals(1, qc.size());
	}

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/simpleTest/test2.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/simpleTest/test2.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/simpleTest/test2.xml")
	public void test2() {
		QueryCondition qc = new QueryCondition();
		qc.setConnectId(111L);
		List<Query> list = queryDao.selectAll(qc);
		System.out.println(JSONObject.toJSONString(list));

		QueryCondition qc2 = new QueryCondition();
		Connect c = new Connect();
		c.setId(111L);
		qc2.setConnect(c);
		List<Query> list2 = queryDao.selectAll(qc2);
		System.out.println(JSONObject.toJSONString(list2));
	}

	@Test
	public void testAddZeroForNum() {
		Integer i1 = Integer.MAX_VALUE;
		String s1 = CommonUtils.addZeroForNum(i1);
		System.out.println(s1);
		Long i2 = Long.MAX_VALUE;
		String s2 = CommonUtils.addZeroForNum(i2);
		System.out.println(s2);
	}
}
