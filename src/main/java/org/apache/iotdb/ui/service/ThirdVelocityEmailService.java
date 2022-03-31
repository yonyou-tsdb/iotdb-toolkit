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
package org.apache.iotdb.ui.service;

import java.io.File;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

@Service
public class ThirdVelocityEmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private VelocityEngine velocityEngine;

	private String systemEmail;

	public void sendEmail(final Map<String, Object> model, final String subject, final String vmfile,
			final String[] mailTo, final String[] files) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
				message.setTo(mailTo);
				message.setSubject(subject);
				message.setFrom(systemEmail);
				String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, vmfile, "utf-8", model);
				message.setText(text, true);
				FileSystemResource file;
				for (String s : files) {
					file = new FileSystemResource(new File(s));
					message.addAttachment(s, file);
				}
			}
		};

		javaMailSender.send(preparator);
	}

	public void sendEmail(final Map<String, Object> model, final String subject, final String vmfile,
			final String[] mailTo, final String[] files, final String mailFrom) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
				message.setTo(mailTo);
				message.setSubject(subject);
				message.setFrom(mailFrom);
				String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, vmfile, "utf-8", model);
				message.setText(text, true);
				FileSystemResource file;
				for (String s : files) {
					file = new FileSystemResource(new File(s));
					message.addAttachment(s, file);
				}
			}
		};

		javaMailSender.send(preparator);
	}

	public String getSystemEmail() {
		return systemEmail;
	}

	public void setSystemEmail(String systemEmail) {
		this.systemEmail = systemEmail;
	}

}
