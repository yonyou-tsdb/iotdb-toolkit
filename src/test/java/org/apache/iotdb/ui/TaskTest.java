package org.apache.iotdb.ui;

import org.apache.iotdb.ui.entity.Task;
import org.apache.iotdb.ui.mapper.TaskDao;
import org.apache.iotdb.ui.model.TaskFlag;
import org.apache.iotdb.ui.model.TaskStatus;
import org.apache.iotdb.ui.model.TaskType;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class, databaseConnection = { "dataSource1" })
public class TaskTest {

	@Autowired
	private TaskDao taskDao;

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/taskTest/test1.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/taskTest/test1.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/taskTest/test1.xml")
	public void test1() {
		Assert.assertNotNull(taskDao);
		Task task = taskDao.select(111L);
		Assert.assertEquals(TaskType.EXPORT, task.getType());
		Assert.assertEquals(TaskStatus.NOT_START, task.getStatus());
		Assert.assertEquals("1", task.getSetting().get("setting"));
		Assert.assertEquals(123, task.getResultRows().intValue());
		Assert.assertEquals("asd", task.getName());
		Assert.assertEquals(1001, task.getTimeCost().intValue());
		Assert.assertEquals(TaskFlag.LONG_TERM, task.getFlag());
		Assert.assertEquals("0/1 * * * * *", task.getExpression());
		Assert.assertEquals(234, task.getLongTermTaskId().intValue());
		task.setStatus(TaskStatus.NORMAL_END);
		task.getSetting().put("setting", "3");
		taskDao.update(task);
	}

}
