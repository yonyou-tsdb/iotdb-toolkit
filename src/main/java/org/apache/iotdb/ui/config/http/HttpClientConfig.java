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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

	private static Logger logger = LoggerFactory.getLogger(HttpClientConfig.class);

	@Value("${iotdbui.httpClient.connectTimeout:20000}")
	private int connectTimeout;

	@Value("${iotdbui.httpClient.connectRequestTimeout:20000}")
	private int connectRequestTimeout;

	@Value("${iotdbui.httpClient.socketTimeout:20000}")
	private int socketTimeout;

	@Value("${iotdbui.httpClient.sslProtocol:SSLv3}")
	private String sslProtocol;

	private int connMaxTotal = 20;

	private int maxPerRoute = 20;

	private int retryTime = 3;

	@Bean
	public RequestConfig config() {
		return RequestConfig.custom().setConnectionRequestTimeout(this.connectRequestTimeout)
				.setConnectTimeout(this.connectTimeout).setSocketTimeout(this.socketTimeout).build();
	}

	@Bean
	public PoolingHttpClientConnectionManager poolingClientConnectionManager()
			throws KeyManagementException, NoSuchAlgorithmException {

		SSLContext sslcontext = createIgnoreVerifySSL();
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();

		PoolingHttpClientConnectionManager poolHttpcConnManager = new PoolingHttpClientConnectionManager(
				socketFactoryRegistry);
		poolHttpcConnManager.setMaxTotal(this.connMaxTotal);
		poolHttpcConnManager.setDefaultMaxPerRoute(this.maxPerRoute);
		return poolHttpcConnManager;
	}

	private SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance(sslProtocol);
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	@Bean
	public HttpRequestRetryHandler httpRequestRetryHandler() {
		final int retryTime2 = this.retryTime;
		return new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				if (executionCount >= retryTime2) {
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					return true;
				}
				if (exception instanceof InterruptedIOException) {
					return true;
				}
				if (exception instanceof UnknownHostException) {
					return false;
				}
				if (exception instanceof ConnectTimeoutException) {
					return false;
				}
				if (exception instanceof SSLException) {
					return false;
				}
				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpRequest request = clientContext.getRequest();
				return (request instanceof HttpEntityEnclosingRequest) ? false : true;
			}
		};
	}

	@Bean("connectionKeepAliveStrategy")
	public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
		return new ConnectionKeepAliveStrategy() {

			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				HeaderElementIterator it = new BasicHeaderElementIterator(
						response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && param.equalsIgnoreCase("timeout")) {
						try {
							return Long.parseLong(value) * 1000;
						} catch (NumberFormatException e) {
							logger.error(e.getMessage());
						}
					}
				}
				return (30 * 1000L);
			}
		};
	}
}
