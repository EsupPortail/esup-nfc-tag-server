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

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;

public class TagIdCheckRestWs implements TagIdCheckApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
    protected RestTemplate restTemplate;

    protected String tagIdCheckUrl;
    
    protected String description;
    
    protected String idFromEppnInitUrl;
    
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTagIdCheckUrl(String tagIdCheckUrl) {
		this.tagIdCheckUrl = tagIdCheckUrl;
	}
	
	public void setIdFromEppnInitUrl(String idFromEppnInitUrl) {
		this.idFromEppnInitUrl = idFromEppnInitUrl;
	}

	@Override
	public TagLog getTagLogFromTagId(TagType tagType, String tagId, String appName) throws EsupNfcTagException {
		
		URI targetUrl = null;
		
		switch (tagType) {
			case CSN :
				targetUrl= UriComponentsBuilder.fromUriString(tagIdCheckUrl)
					    .queryParam("csn", tagId)    
					    .build()
					    .toUri();
				log.info("Call " + tagIdCheckUrl + " with csn = " + tagId);
				break;
			case DESFIRE :
				String desfireId = tagId;
				targetUrl= UriComponentsBuilder.fromUriString(tagIdCheckUrl)
					    .queryParam("desfireId", desfireId)   
					    .queryParam("appName", appName)
					    .build()
					    .toUri();
				log.info("Call " + tagIdCheckUrl + " with desfireId = " + desfireId);
				break;
			default:
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_typenotsupported, tagType.toString());
		}		
		
		TagLog tagLog = null;
		try {
			tagLog = restTemplate.getForObject(targetUrl, TagLog.class);
		} catch(RestClientException e){
			log.warn("tagIdCheck unavailable", e);
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
		}
		log.info("Got :  " + tagLog);
		
		if(tagLog==null) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowcard);
		}

		return tagLog;	
	}
}
