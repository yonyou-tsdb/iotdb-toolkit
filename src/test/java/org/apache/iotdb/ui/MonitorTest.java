package org.apache.iotdb.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class MonitorTest {

	@Autowired
	@Qualifier("httpClientBean")
	private CloseableHttpClient HttpClientBean;

	@Test
	public void test0() {
		Assert.assertNotNull(HttpClientBean);
		try {
			readMetrics();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readMetrics() throws Exception {
		HttpGet http = new HttpGet("http://172.20.48.111:9091/metrics");
		// 发送请求，获取服务器返回的httpResponse对象
		try (CloseableHttpResponse httpResponse = HttpClientBean.execute(http);
				// 用输入流获取，字节读取
				InputStream inputStream = httpResponse.getEntity().getContent();
				// 转换成字符流
				InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
				// 缓冲字符流，提供字符、数组和行的高效读取
				BufferedReader br = new BufferedReader(reader);) {
			// 行读取
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		}
	}
}
