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
package org.apache.iotdb.ui.config.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("httpClientBean")
public class HttpClientBean implements FactoryBean<CloseableHttpClient>, InitializingBean, DisposableBean {

	private CloseableHttpClient client;

	@Autowired
	private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

	@Autowired
	private HttpRequestRetryHandler httpRequestRetryHandler;

	@Autowired
	private PoolingHttpClientConnectionManager poolHttpcConnManager;

	@Autowired
	private RequestConfig config;

	@Override
	public void destroy() throws Exception {
		if (null != this.client) {
			this.client.close();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.client = HttpClients.custom().setConnectionManager(poolHttpcConnManager)
				.setRetryHandler(httpRequestRetryHandler).setKeepAliveStrategy(connectionKeepAliveStrategy)
				.setDefaultRequestConfig(config).build();
	}

	@Override
	public CloseableHttpClient getObject() throws Exception {
		return this.client;
	}

	@Override
	public Class<?> getObjectType() {
		return (this.client == null ? CloseableHttpClient.class : this.client.getClass());
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
