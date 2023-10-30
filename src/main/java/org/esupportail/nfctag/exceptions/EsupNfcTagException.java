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
package org.esupportail.nfctag.exceptions;

import org.esupportail.nfctag.beans.ApplicationContextProvider;
import org.esupportail.nfctag.beans.NfcResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;

public class EsupNfcTagException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	static public enum EsupNfcTagErrorMessage{
		error_esupnfctagexception_serviceunavailable,
		error_esupnfctagexception_unknowcard,
		error_esupnfctagexception_nottagable,
		error_esupnfctagexception_typenotsupported,
		error_esupnfctagexception_unknowdevice,
		error_esupnfctagexception_locationnotfound,
		error_esupnfctagexception_tagidchecknotdefine,
		error_esupnfctagexception_tagvalidationerror,
		error_esupnfctagexception_httperror
	}
	
	String[] params;
	
	String message;
	
	String numeroId;

	NfcResultBean.CODE code = NfcResultBean.CODE.ERROR;
	
	public EsupNfcTagException(Throwable cause, EsupNfcTagErrorMessage message, String...params ) {
		super(message.toString(), cause);
		this.message = message.toString();
		this.params = params;
	}
	
	public EsupNfcTagException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	public EsupNfcTagException(EsupNfcTagErrorMessage message, String...params) {
		super(message.toString());
		this.message = message.toString();
		this.params = params;
	}

	public EsupNfcTagException(EsupNfcTagErrorMessage message) {
		super(message.toString());
		this.message = message.toString();
	}
	
	public EsupNfcTagException(String message, String numeroId) {
		super(message);
		this.message = message;
		this.numeroId = numeroId;
	}

	public EsupNfcTagException(String message, String numeroId, NfcResultBean.CODE code) {
		super(message);
		this.message = message;
		this.numeroId = numeroId;
		this.code = code;
	}
	
	@Override
	public String getMessage() {
		try {
			ReloadableResourceBundleMessageSource messageSource = ApplicationContextProvider.getApplicationContext().getBean("messageSource", ReloadableResourceBundleMessageSource.class);
			LocaleResolver localeResolver = (LocaleResolver)ApplicationContextProvider.getApplicationContext().getBean("localeResolver");
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			return messageSource.getMessage(this.message, this.params, localeResolver.resolveLocale(request));
		} catch(Exception e) {
			log.debug("Exception during computing exception message (" + this.message + ") with localisation : " + e.getMessage());
			return this.message;
		}
	}

	public String getNumeroId() {
		return numeroId;
	}

	public NfcResultBean.CODE getCode() {
		return code;
	}
}
