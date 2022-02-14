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

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.io.VFS;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@MapperScan(basePackages = { "org.apache.iotdb.ui.mapper" }, sqlSessionFactoryRef = "sqlSessionFactory1")
public class SqlSessionFactoryConfig {

	@ConfigurationProperties(prefix = "spring.data-source1")
	@Bean("dataSource1")
	@Primary
	public DataSource dataSource1() {
		return DataSourceBuilder.create().type(com.alibaba.druid.pool.DruidDataSource.class).build();
	}

	@Bean(name = "sqlSessionFactory1")
	@Primary
	public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("dataSource1") DataSource dataSource)
			throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		// 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
		sessionFactory.setDataSource(dataSource);
		VFS.addImplClass(SpringBootVFS.class);
		sessionFactory.setTypeAliasesPackage("org.apache.iotdb.ui.entity"); // 扫描Model
		sessionFactory.setConfigLocation(new ClassPathResource("Configuration.xml"));
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] ra2 = resolver.getResources("classpath*:org/apache/iotdb/ui/mapper/*.xml");
		Resource[] ra = (Resource[]) ArrayUtils.addAll(null, ra2);
		sessionFactory.setMapperLocations(ra); // 扫描映射文件
		return sessionFactory;
	}

	@Bean(name = "transactionManager1")
	public PlatformTransactionManager transactionManager(@Qualifier("dataSource1") DataSource dataSource) {
		// 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
		return new DataSourceTransactionManager(dataSource);
	}
}
