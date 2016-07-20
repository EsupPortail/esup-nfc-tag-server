package org.esupportail.nfctag.service.api.impl;

import java.util.List;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AppliExtRestWs implements AppliExtApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
    protected RestTemplate restTemplate;
    
    protected String description;
    
    protected String header;
    
    protected String backgroundColor;

	protected String isTagableUrl;

	protected String validateTagUrl;
    
    protected String getLocationsUrl;

    public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setIsTagableUrl(String isTagableUrl) {
		this.isTagableUrl = isTagableUrl;
	}

	public void setValidateTagUrl(String validateTagUrl) {
		this.validateTagUrl = validateTagUrl;
	}
	
	public void setGetLocationsUrl(String getLocationsUrl) {
		this.getLocationsUrl = getLocationsUrl;
	}

	public AppliExtRestWs() {
		restTemplate = new RestTemplate();
	}

	@Override
	public List<String> getLocations4Eppn(String eppn) {
		log.debug("getLocation for : " + eppn);
		try {
			return restTemplate.getForObject(getLocationsUrl + "?eppn=" + eppn, List.class);
		} catch(HttpServerErrorException e) {
			log.error("HttpServerErrorException on "+getLocationsUrl+" - "+ e.getMessage() +" - "+e.getStackTrace());
			return null;
		} catch(RestClientException e) {
			log.error("RestClientException on "+getLocationsUrl + " - "+ e.getMessage() +" - "+e.getStackTrace());
			return null;			
		}
	}	
	
	@Override
	public void isTagable(TagLog tagLog) throws EsupNfcTagException {
		log.trace("isTagable : " + tagLog);
		try {
			restTemplate.postForLocation(isTagableUrl, tagLog);
		} catch(HttpServerErrorException e) {
			if(e.getStatusCode()==HttpStatus.SERVICE_UNAVAILABLE){
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable);
			} else {
				log.warn("HttpServerErrorException : " + e.getMessage() + " - " + e.getResponseBodyAsString());
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_httperror, e.getResponseBodyAsString());	
			}
			
		}
	}

	@Override
	public boolean validateTag(TagLog tagLog) {
		log.trace("validateTag : " + tagLog);
		try {
			restTemplate.postForLocation(validateTagUrl, tagLog);
			return true;
		} catch(HttpServerErrorException e) {
			
			if(e.getStatusCode()!=HttpStatus.SERVICE_UNAVAILABLE) {
				log.warn("HttpServerErrorException : " + e.getMessage() + " - " + e.getResponseBodyAsString());

			}
			return false;
		}
	}

	@Override
	public boolean cancelTag(TagLog tagLog) {
		return true;
	}
	
}
