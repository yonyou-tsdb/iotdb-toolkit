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
package org.apache.iotdb.ui.config.tsdatasource;

import javax.servlet.http.HttpServletRequest;

import org.apache.iotdb.ui.util.IpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Order(-1) // 该切面应当先于 @Transactional 执行
@Component
public class DynamicDataSourceAspect {

	public static Long getTenantCode(HttpServletRequest request) {
		String ret = IpUtils.getCookieValue(request,
				new StringBuilder(IpUtils.getCookieValue(request, "JSESSIONID")).append("-tenantId").toString());
		return ret == null ? null : Long.parseLong(ret);
	}

	private HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		return request;
	}

	@Before("execution(public * org.apache.iotdb.ui..*.*WithTenant(..))")
	public void beforeWithTenant() throws Throwable {
		HttpServletRequest request = getRequest();
		Long tId = getTenantCode(request);
		if (tId != null) {
			DynamicTSDataSourceContextHolder.setSessionPoolKey(tId);
		} else {
			DynamicTSDataSourceContextHolder.clearSessionPoolKey();
		}
	}

	@After("execution(public * org.apache.iotdb.ui..*.*WithTenant(..))")
	public void afterWithTenant(JoinPoint joinPoint) throws Throwable {
		DynamicTSDataSourceContextHolder.clearSessionPoolKey();
	}

}
