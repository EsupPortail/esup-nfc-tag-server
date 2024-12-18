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

import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TagWriteRestWs implements TagWriteApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/* cache Ids with FIFO Map 
	 * This allow faster operation.
	 * But this allows also that big data (multiple frames) is not altered during the writing !
	 * */
	private Map<URI, String> cacheIdsMap = new LinkedHashMap<URI, String>(200) {

		private static final long serialVersionUID = 1L;

		@Override
	    protected boolean removeEldestEntry(Entry<URI, String> eldest) {
	        return size() > 200;
	    }
	};

	@Resource
	protected RestTemplate restTemplate;

	protected String idFromCsnUrlTemplate;

	protected String getIdFromCsnUrl(String csn){
		String url = MessageFormat.format(idFromCsnUrlTemplate, csn);
		return url;
	}

	public void setIdFromCsnUrlTemplate(String idFromCsnUrlTemplate) {
		this.idFromCsnUrlTemplate = idFromCsnUrlTemplate;
	}


	@Override
	public String getIdFromCsn(String csn) {
		URI targetUrl= UriComponentsBuilder.fromUriString(getIdFromCsnUrl(csn))
			    .build()
			    .toUri();	
		String id = cacheIdsMap.get(targetUrl);
		if(id == null) {
			log.trace("Call " + getIdFromCsnUrl(csn) + " with csn = " + csn);
			id = callUrlGetForObject(targetUrl);
		} else {
			log.trace("Cache for " + getIdFromCsnUrl(csn) + " with csn = " + csn + " -> " + id);
		}
		return id;
	}

	private String callUrlGetForObject(URI targetUrl) {
		String id;
		try {
			id = restTemplate.getForObject(targetUrl, String.class);
			cacheIdsMap.put(targetUrl, id);
		} catch(HttpStatusCodeException e){
			log.warn("tagIdCheck error : " + targetUrl);
			HttpStatus status = e.getStatusCode();
			if (!HttpStatus.NOT_FOUND.equals(status)) {
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
			} else {
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowcard, targetUrl.toString());
			}
		}
		log.trace("Got :  " + id);
		return id;
	}

}
