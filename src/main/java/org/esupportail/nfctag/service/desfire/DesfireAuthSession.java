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
package org.esupportail.nfctag.service.desfire;

import java.io.Serializable;

import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value="session")
public class DesfireAuthSession implements Serializable {

	private static final long serialVersionUID = 1L;
	
	NfcAuthConfig desfireAuthConfig;

	private String rndBPrimEnc;
	
	private String rndA;
	
	private String rndBdecrypt;
	
	private String sessionKey;
	
	public String getRndBPrimEnc() {
		return rndBPrimEnc;
	}
	public void setRndBPrimEnc(String rndBPrimEnc) {
		this.rndBPrimEnc = rndBPrimEnc;
	}
	public String getRndA() {
		return rndA;
	}
	public void setRndA(String rndA) {
		this.rndA = rndA;
	}
	public String getRndBdecrypt() {
		return rndBdecrypt;
	}
	public void setRndBdecrypt(String rndBdecrypt) {
		this.rndBdecrypt = rndBdecrypt;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public NfcAuthConfig getDesfireAuthConfig() {
		return desfireAuthConfig;
	}
	public void setDesfireAuthConfig(NfcAuthConfig desfireAuthConfig) {
		this.desfireAuthConfig = desfireAuthConfig;
	}
}
