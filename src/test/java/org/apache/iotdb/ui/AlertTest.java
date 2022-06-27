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

import org.apache.iotdb.ui.entity.Alert;
import org.apache.iotdb.ui.entity.Trigger;
import org.apache.iotdb.ui.mapper.AlertDao;
import org.apache.iotdb.ui.mapper.TriggerDao;
import org.apache.iotdb.ui.model.AlertStatus;
import org.apache.iotdb.ui.model.TriggerStatus;
import org.apache.iotdb.ui.service.TriggerService;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader;

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class, databaseConnection = { "dataSource1" })
public class AlertTest {

	@Autowired
	private AlertDao alertDao;

	@Autowired
	private TriggerDao triggerDao;

	@Autowired
	private TriggerService triggerService;

	@Test
	public void test0() {
		Assert.assertTrue(true);
		Assert.assertNotNull(alertDao);
	}

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/alertTest/test1.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/alertTest/test1.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/alertTest/test1.xml")
	public void test1() {
		Alert alert = alertDao.select(211L);
		Assert.assertEquals("code1", alert.getCode());
		Assert.assertEquals(AlertStatus.DEVELOP, alert.getStatus());

		Trigger trigger = triggerDao.select(112L);
		Assert.assertEquals(TriggerStatus.ENABLE, trigger.getStatus());
		Assert.assertEquals("code2", trigger.getAlert().getCode());
		Assert.assertEquals(AlertStatus.DEPLOYED, trigger.getAlert().getStatus());

		Trigger t = new Trigger();
		t.setStatus(TriggerStatus.ENABLE);
		triggerService.loadAlert(alert, t);
		Assert.assertEquals(1, alert.getTrigger().size());
		for (Trigger e : (Collection<Trigger>) alert.getTrigger()) {
			Assert.assertEquals("name3", e.getName());
		}

		Alert alert2 = new Alert();
		alert2.setOrigin(1L);
		alertDao.insert(alert2);
		Alert a = new Alert();
		int c = alertDao.count(a);
		Assert.assertEquals(3, c);
		alertDao.delete(alert2);
	}
}
