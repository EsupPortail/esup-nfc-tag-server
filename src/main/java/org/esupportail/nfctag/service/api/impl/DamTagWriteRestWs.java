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
import org.esupportail.nfctag.service.api.DamTagWriteApi;
import org.esupportail.nfctag.web.wsrest.json.JsonDamAuthKey;
import org.esupportail.nfctag.web.wsrest.json.JsonFormCryptogram;
import org.esupportail.nfctag.web.wsrest.json.JsonResponseCryptogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.Resource;
import java.net.URI;
import java.text.MessageFormat;

public class DamTagWriteRestWs implements DamTagWriteApi {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected RestTemplate restTemplate;

	protected String damAuthKeyUrlTemplate;

	protected String cryptogramUrlTemplate;

	public String getDamAuthKeyFromCsnUrl(String csn) {
		return MessageFormat.format(damAuthKeyUrlTemplate, csn);
	}

	public void setDamAuthKeyUrlTemplate(String damAuthKeyUrlTemplate) {
		this.damAuthKeyUrlTemplate = damAuthKeyUrlTemplate;
	}

	public String getCryptogramUrl() {
		return cryptogramUrlTemplate;
	}

	public void setCryptogramUrlTemplateUrlTemplate(String cryptogramUrlTemplate) {
		this.cryptogramUrlTemplate = cryptogramUrlTemplate;
	}


	@Override
	public JsonDamAuthKey getDamAuthKey(String csn) throws EsupNfcTagException {
		URI targetUrl= UriComponentsBuilder.fromUriString(getDamAuthKeyFromCsnUrl(csn))
				.build()
				.toUri();

		log.trace("Call " + getDamAuthKeyFromCsnUrl(csn) + " with csn = " + csn);

		return restTemplate.getForObject(targetUrl, JsonDamAuthKey.class);
	}

	@Override
	public JsonResponseCryptogram getCryptogram(JsonFormCryptogram jsonFormCryptogram) throws EsupNfcTagException {
		URI targetUrl= UriComponentsBuilder.fromUriString(getCryptogramUrl())
				.build()
				.toUri();

		log.trace("Call " + getCryptogramUrl() + " with form = " + jsonFormCryptogram);

		try {
			JsonResponseCryptogram jsonResponseCryptogram = restTemplate.postForObject(targetUrl, jsonFormCryptogram, JsonResponseCryptogram.class);
			log.trace("Got :  " + jsonResponseCryptogram);
			return jsonResponseCryptogram;
		} catch(HttpStatusCodeException e){
			log.warn("tagIdCheck error : " + targetUrl);
			HttpStatusCode status = e.getStatusCode();
			if (!HttpStatus.NOT_FOUND.equals(status)) {
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
			} else {
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowcard);
			}
		}
	}
}
