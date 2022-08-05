package org.apache.iotdb.ui;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.iotdb.ui.config.DynamicTask;
import org.apache.iotdb.ui.config.DynamicTask.TaskConstant;
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
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class, databaseConnection = { "dataSource1" })
public class DynamicTaskTest {

	@Autowired
	private DynamicTask dynamicTask;

	@Test
	public void test() throws InterruptedException {

//		List<DynamicTask.TaskConstant> taskConstans = dynamicTask.getTaskConstants();
		ConcurrentHashMap<String, TaskConstant> taskConstantMap = dynamicTask.getTaskConstantMap();
//		DynamicTask.TaskConstant taskConstant = new DynamicTask.TaskConstant();
//		taskConstant.setCron("0/5 * * * * ?");
//		taskConstant.setTaskId("test1");
//		taskConstant.setRule("每隔5秒执行");
//		taskConstantMap.put("test1", taskConstant);

//		DynamicTask.TaskConstant taskConstant1 = new DynamicTask.TaskConstant();
//		taskConstant1.setCron("0/8 * * * * ?");
//		taskConstant1.setTaskId("test2");
//		taskConstant1.setRule("每隔8秒执行");
//		taskConstans.add(taskConstant1);
//
		DynamicTask.TaskConstant taskConstant2 = new DynamicTask.TaskConstant();
		taskConstant2.setCron("0/15 * * * * ?");
		taskConstant2.setTaskId("test3");
		taskConstant2.setRule("每隔15秒执行");
//		taskConstantMap.put("test3", taskConstant2);
		dynamicTask.addTask(taskConstant2);
		System.out.println(taskConstantMap.size());
		TimeUnit.SECONDS.sleep(20);
		// 更新test1的定时任务配置
		DynamicTask.TaskConstant taskConstant4 = new DynamicTask.TaskConstant();
		taskConstant4.setCron("0/6 * * * * ?");
		taskConstant4.setTaskId("test1");
		taskConstant4.setRule("每隔6秒执行");
		dynamicTask.updateTask("test1", taskConstant4);
//		taskConstantMap.put("test1", taskConstant4);

//		TimeUnit.SECONDS.sleep(20);
//		DynamicTask.TaskConstant taskConstant3 = new DynamicTask.TaskConstant();
//		taskConstant3.setCron("0/20 * * * * ?");
//		taskConstant3.setTaskId("test4");
//		taskConstant3.setRule("每隔20秒执行");
//		taskConstans.add(taskConstant3);
		System.out.println(taskConstantMap.size());
		TimeUnit.SECONDS.sleep(20);

//		taskConstantMap.remove("test1");
		dynamicTask.deleteTask("test1");
		System.out.println(taskConstantMap.size());
		TimeUnit.SECONDS.sleep(20);
	}
}
