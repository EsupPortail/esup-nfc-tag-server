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
package org.esupportail.nfctag.service.api.impl;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class DesfireReadConfig extends NfcAuthConfig {
	
	private String desfireAppName;
	
	private String desfireAppId;

	private String desfireKeyNumber;
	
	private String desfireKey;
	
	private String desfireFileNumber;
	
	private String desfireFileOffset;
	
	private String desfireFileSize;
	
	@Override
	public AuthType getAuthType() {
		return AuthType.DESFIRE;
	}

		public String getDesfireAppName() {
		return desfireAppName;
	}

	public void setDesfireAppName(String desfireAppName) {
		this.desfireAppName = desfireAppName;
	}

	public String getDesfireAppId() {
		return desfireAppId;
	}

	public void setDesfireAppId(String desfireAppId) {
		this.desfireAppId = desfireAppId;
	}

	public String getDesfireKeyNumber() {
		return desfireKeyNumber;
	}

	public void setDesfireKeyNumber(String desfireKeyNumber) {
		this.desfireKeyNumber = desfireKeyNumber;
	}

	public String getDesfireKey() {
		return desfireKey;
	}

	public void setDesfireKey(String desfireKey) {
		this.desfireKey = desfireKey;
	}

	public String getDesfireFileNumber() {
		return desfireFileNumber;
	}

	public void setDesfireFileNumber(String desfireFileNumber) {
		this.desfireFileNumber = desfireFileNumber;
	}
	
	public String getDesfireFileOffset() {
		return desfireFileOffset;
	}

	public void setDesfireFileOffset(String desfireFileOffset) {
		this.desfireFileOffset = desfireFileOffset;
	}

	public String getDesfireFileSize() {
		return desfireFileSize;
	}

	public void setDesfireFileSize(String desfireFileSize) {
		this.desfireFileSize = desfireFileSize;
	}
}
