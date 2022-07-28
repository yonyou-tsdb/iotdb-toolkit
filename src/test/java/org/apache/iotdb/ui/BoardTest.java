package org.apache.iotdb.ui;

import java.util.Collection;

import org.apache.iotdb.ui.config.ExporterThreadPoolConfig;
import org.apache.iotdb.ui.entity.Board;
import org.apache.iotdb.ui.entity.Exporter;
import org.apache.iotdb.ui.entity.Panel;
import org.apache.iotdb.ui.mapper.BoardDao;
import org.apache.iotdb.ui.mapper.ExporterDao;
import org.apache.iotdb.ui.mapper.PanelDao;
import org.apache.iotdb.ui.service.PanelService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
public class BoardTest {

	@Autowired
	private BoardDao boardDao;

	@Autowired
	private ExporterDao exporterDao;

	@Autowired
	private PanelDao panelDao;

	@Autowired
	private PanelService panelService;

	@Autowired
	private ExporterThreadPoolConfig exporterThreadPoolConfig;

	@Autowired
	@Qualifier("exporterTaskExecutor")
	private ThreadPoolTaskExecutor exporterTaskExecutor;

	@Test
	public void test0() {
		Assert.assertTrue(true);
		Assert.assertNotNull(panelService);
		Assert.assertNotNull(exporterThreadPoolConfig);
		Assert.assertNotNull(exporterTaskExecutor);
	}

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/boardTest/test1.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/boardTest/test1.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/boardTest/test1.xml")
	public void test1() {
		Board board = boardDao.select(211L);
		Assert.assertEquals("name1", board.getName());

		Panel panel = panelDao.select(112L);
		Assert.assertEquals("setting2", panel.getSetting());
		Assert.assertEquals("name2", panel.getBoard().getName());

		Panel m = new Panel();
		m.setQuery("query2");
		panelService.loadBoard(board, m);
		Assert.assertEquals(1, board.getPanel().size());
		for (Panel e : (Collection<Panel>) board.getPanel()) {
			Assert.assertEquals("setting3", e.getSetting());
		}

		Board board2 = new Board();
		board2.setName("b");
		boardDao.insert(board2);
		Board b = new Board();
		int c = boardDao.count(b);
		Assert.assertEquals(3, c);
		boardDao.delete(board2);
	}

	@Test
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/org/apache/iotdb/ui/boardTest/test2.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, value = "/org/apache/iotdb/ui/boardTest/test2.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/org/apache/iotdb/ui/boardTest/test2.xml")
	public void test2() {
		Exporter exporter = exporterDao.select(112L);
		Assert.assertEquals("name2", exporter.getName());

		Exporter exporter2 = new Exporter();
		exporter2.setName("b");
		exporterDao.insert(exporter2);
		Exporter b = new Exporter();
		int c = exporterDao.count(b);
		Assert.assertEquals(3, c);
		exporterDao.delete(exporter2);
	}
}
