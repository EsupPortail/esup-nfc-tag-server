/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.logs;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RemoteConverter extends ClassicConverter {
    
	final static String NO_REMOTE_ADDRESS = "NO_REMOTE_ADDRESS";
	
	@Override
	public String convert(ILoggingEvent event) {
		String remoteAddress = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
				.getRequest().getRemoteAddr();
		if (remoteAddress != null) {
			return remoteAddress;
		}
		return NO_REMOTE_ADDRESS;     
	}
}