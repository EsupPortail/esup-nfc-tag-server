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

import java.net.URI;
import java.text.MessageFormat;

import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class TagWriteRestWs implements TagWriteApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
    protected RestTemplate restTemplate;
    
    protected String idFromCsnUrlTemplate;
	
    protected String getIdFromCsnUrl(String csn){
    	String url = MessageFormat.format(idFromCsnUrlTemplate, csn);
    	return url;
    }
    
	public void setIdFromCsnUrlTemplate(String idFromCsnUrlTemplate) {
		this.idFromCsnUrlTemplate = idFromCsnUrlTemplate;
	}

	public TagWriteRestWs() {
		restTemplate = new RestTemplate();
	}

	@Override
	public String getIdFromCsn(String csn) {
		URI targetUrl= UriComponentsBuilder.fromUriString(getIdFromCsnUrl(csn))
			    .build()
			    .toUri();
		log.trace("Call " + getIdFromCsnUrl(csn) + " with csn = " + csn);

		String id = null;
		try {
			id = restTemplate.getForObject(targetUrl, String.class);
		} catch(RestClientException e){
			log.warn("tagIdCheck unavailable", e);
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
		}
		log.trace("Got :  " + id);
		if(id==null) {
			//throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowcard);
		}
		return id;
	}
	
}
