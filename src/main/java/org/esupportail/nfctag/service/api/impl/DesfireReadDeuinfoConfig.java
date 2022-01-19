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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.desfire.DesfireDiversification;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.DesfireUtils;
import org.esupportail.nfctag.service.desfire.actions.DesfireActionService;
import org.esupportail.nfctag.service.desfire.actions.DesfireDeuinfoActionService;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DesfireReadDeuinfoConfig extends DesfireReadConfig {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	String baseKey;
	
	public String getBaseKey() {
		return baseKey;
	}

	public void setBaseKey(String baseKey) {
		this.baseKey = baseKey;
	}

	@Override
	public DesfireActionService getDesfireActionService(DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
		return new DesfireDeuinfoActionService(this, desfireService, tagAuthService, liveController);
	}

	public String getDesfireDiversifiedKey(String csnOrEscn) {

		String diversifiedKey = null;

		byte[] baseKeyBytes = DesfireUtils.hexStringToByteArray(baseKey);
		byte[] diversificationInput = DesfireUtils.hexStringToByteArray(csnOrEscn);
		int diversificationLength = diversificationInput.length;
		
		DesfireDiversification desfireDiversification = new DesfireDiversification();
		
		try {
			byte[] diversifiedKeyBytes = desfireDiversification.diversificationAES128(baseKeyBytes, diversificationInput, diversificationLength);
			diversifiedKey = DesfireUtils.byteArrayToHexString(diversifiedKeyBytes);
		} catch (Exception e) {
			throw new EsupNfcTagException("Exception when diversify key !", e);
		}
		log.info(String.format("Diversified Key : %s", diversifiedKey));
		
		return diversifiedKey;
	}

    public Logger getLog() {
        return this.log;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
