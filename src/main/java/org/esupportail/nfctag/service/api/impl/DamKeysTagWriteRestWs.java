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

import javax.annotation.Resource;

import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.DamKeysTagWriteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class DamKeysTagWriteRestWs implements DamKeysTagWriteApi {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected RestTemplate restTemplate;

	protected String createDamKeysFromCsnUrlTemplate;

	protected String damKeysFromCsnUrlTemplate;

	protected String resetDamKeysUrlTemplate;


	public String getCreateDamKeysFromCsnUrl(String csn) {
		return MessageFormat.format(createDamKeysFromCsnUrlTemplate, csn);
	}

	public void setCreateDamKeysFromCsnUrlTemplate(String createDamKeysFromCsnUrlTemplate) {
		this.createDamKeysFromCsnUrlTemplate = createDamKeysFromCsnUrlTemplate;
	}

	public String getDamKeysFromCsnUrl(String csn) {
		return MessageFormat.format(damKeysFromCsnUrlTemplate, csn);
	}

	public void setDamKeysFromCsnUrlTemplate(String damKeysFromCsnUrlTemplate) {
		this.damKeysFromCsnUrlTemplate = damKeysFromCsnUrlTemplate;
	}

	public String getResetDamKeysUrl(String csn) {
		return MessageFormat.format(resetDamKeysUrlTemplate, csn);
	}

	public void setResetDamKeysUrlTemplate(String resetDamKeysUrlTemplate) {
		this.resetDamKeysUrlTemplate = resetDamKeysUrlTemplate;
	}


	@Override
	public String createDiversDamKey(String csn) throws EsupNfcTagException {
		URI targetUrl= UriComponentsBuilder.fromUriString(getCreateDamKeysFromCsnUrl(csn))
				.build()
				.toUri();

		log.trace("Call " + getCreateDamKeysFromCsnUrl(csn) + " with csn = " + csn);
		return callUrlGetForObject(targetUrl);
	}

	@Override
	public String getDiversDamKey(String csn) throws EsupNfcTagException {
		URI targetUrl= UriComponentsBuilder.fromUriString(getDamKeysFromCsnUrl(csn))
				.build()
				.toUri();

		log.trace("Call " + getDamKeysFromCsnUrl(csn) + " with csn = " + csn);
		return callUrlGetForObject(targetUrl);
	}

	@Override
	public String resetDiversDamKey(String csn) throws EsupNfcTagException {
		URI targetUrl= UriComponentsBuilder.fromUriString(getResetDamKeysUrl(csn))
				.build()
				.toUri();

		log.trace("Call " + getResetDamKeysUrl(csn) + " with csn = " + csn);

		return callUrlGetForObject(targetUrl);
	}


	private String callUrlGetForObject(URI targetUrl) {
		String id;
		try {
			id = restTemplate.getForObject(targetUrl, String.class);
		} catch(HttpStatusCodeException e){
			log.warn("tagIdCheck error : " + targetUrl);
			HttpStatus status = e.getStatusCode();
			if (!HttpStatus.NOT_FOUND.equals(status)) {
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
			} else {
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowcard);
			}
		}
		log.trace("Got :  " + id);
		return id;
	}

}
