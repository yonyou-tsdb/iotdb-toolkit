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
package org.apache.iotdb.ui.config;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 
 * @date 2019年7月8日 11:56:08
 *
 * @author 李萌
 * @Email limeng32@chinaunicom.cn
 * @version
 * @since JDK 1.8
 */
@Configuration
public class EmailConfig {

	@Value("${iotdbui.email.host:}")
	private String host;

	@Value("${iotdbui.email.port:465}")
	private String port;

	@Value("${iotdbui.email.username:}")
	private String username;

	@Value("${iotdbui.email.password:}")
	private String password;

	@Value("${iotdbui.frontend:}")
	private String endPoint;

	@Bean(name = "javaMailSender", value = "javaMailSender")
	public JavaMailSender javaMailSenderImpl() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setProtocol("smtp");
		javaMailSender.setHost(getHost());
		javaMailSender.setUsername(getUsername());
		javaMailSender.setPassword(getPassword());
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.timeout", 25000);
		properties.put("mail.connectiontimeout.timeout", 25000);
		properties.put("mail.writetimeout.timeout", 25000);
		properties.put("mail.smtp.port", getPort());
		properties.put("mail.smtp.socketFactory.port", getPort());
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.ssl.enable", true);
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		javaMailSender.setJavaMailProperties(properties);
		return javaMailSender;
	}

	@Bean(name = "velocityEngine", value = "velocityEngine")
	public VelocityEngine velocityEngineFactoryBean() {
		VelocityEngine velocityEngineFactory = new VelocityEngine();
		velocityEngineFactory.setProperty("resource.loader", "class");
		velocityEngineFactory.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		return velocityEngineFactory;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getEndPointWisely() {
		if ("_iotdbui_frontend_".equals(endPoint)) {
			return "127.0.0.1:80";
		}
		return endPoint;
	}
}