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
import java.util.Date;

import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.TagUpdateApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class TagUpdateRestWs implements TagUpdateApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
    protected RestTemplate restTemplate;
    
    protected String wsUrl;
	
	public void setWsUrl(String wsUrl) {
		this.wsUrl = wsUrl;
	}

	public TagUpdateRestWs() {
		restTemplate = new RestTemplate();
	}

	@Override
	public String getIdFromCsn(String csn) {
		URI targetUrl= UriComponentsBuilder.fromUriString(wsUrl)
			    .queryParam("csn", csn)    
			    .build()
			    .toUri();
		log.info("Call " + wsUrl + " with csn = " + csn);

		String id = null;
		try {
			id = restTemplate.getForObject(targetUrl, String.class);
		} catch(RestClientException e){
			log.warn("tagIdCheck unavailable", e);
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
		}
		log.info("Got :  " + id);
		if(id==null) {
			//throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowcard);
		}
		return id;
	}
	
	@Override
	public Date getLastUpdateDateFromCsn(String csn) {
		URI targetUrl= UriComponentsBuilder.fromUriString(wsUrl)
			    .queryParam("csn", csn)    
			    .build()
			    .toUri();
		log.info("Call " + wsUrl + " with csn = " + csn);

		String stringDate = null;
		try {
			stringDate = restTemplate.getForObject(targetUrl, String.class);
		} catch(RestClientException e){
			log.warn("tagIdCheck unavailable", e);
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
		}
		log.info("Got :  " + stringDate);
		return new Date(Long.parseLong(stringDate));
	}
	
}
