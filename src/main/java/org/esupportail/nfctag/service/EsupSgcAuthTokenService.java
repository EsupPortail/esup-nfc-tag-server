package org.esupportail.nfctag.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class EsupSgcAuthTokenService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
			
	@Resource
    protected RestTemplate restTemplate;
	
    protected String esupSgcUrl;

	public void setEsupSgcUrl(String esupSgcUrl) {
		this.esupSgcUrl = esupSgcUrl;
	}

	public String getAuthToken(String eppnInit) {
		String esupSgcUrl4eppnInit = esupSgcUrl + "/wsrest/nfc/generateAuthToken?eppnInit=" + eppnInit;
		log.info("Call " + esupSgcUrl4eppnInit);
		String authToken = restTemplate.getForObject(esupSgcUrl4eppnInit, String.class);
		log.info("tokken " + authToken);
		return authToken;
	}
	
}
