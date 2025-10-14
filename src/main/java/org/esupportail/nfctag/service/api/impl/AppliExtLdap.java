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
import org.esupportail.nfctag.service.LdapService;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class AppliExtLdap implements AppliExtApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	protected List<LdapService> ldapServices;
	
	@Resource
    protected RestTemplate restTemplate;
    
    protected String description;
    
    protected String header;
    
    protected String backgroundColor;

	protected String isTagableFilter;
	
	protected String locationFilter;

    protected String locationName;
    
    protected String displayAttribut;
    
	public void setLdapServices(List<LdapService> ldapServices) {
		this.ldapServices = ldapServices;
	}

	public String getIsTagableFilter() {
		return isTagableFilter;
	}

	public void setIsTagableFilter(String isTagableFilter) {
		this.isTagableFilter = isTagableFilter;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

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
	
	public void setLocationsName(String getLocationName) {
		this.locationName = getLocationName;
	}

    public void setDisplayAttribut(String displayAttribut) {
		this.displayAttribut = displayAttribut;
	}

	@Override
	public List<String> getLocations4Eppn(String eppn) {
		List<String> locations = new ArrayList<String>();
		for (LdapService ldapService : ldapServices) {
			if(ldapService.isTagable(eppn, locationFilter)){
				locations.add(locationName);
				break;
			}
		}
		return locations; 
	}
	
	@Override
	public void isTagable(TagLog tagLog) throws EsupNfcTagException {
		log.trace("isTagable : " + tagLog);
		for (LdapService ldapService : ldapServices) {
			if(ldapService.isTagable(tagLog.getEppn(), isTagableFilter)){
				return;
			}
		}
		throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_nottagable, "");		
	}

	@Override
	public boolean validateTag(TagLog tagLog) {
		return true;
	}

	@Override
	public boolean cancelTag(TagLog tagLog) {
		return true;
	}
	
	@Override
	public boolean isDisplay(){
		if(displayAttribut != null && displayAttribut != ""){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public String getDisplay(TagLog tagLog){
		for (LdapService ldapService : ldapServices) {
			String display = ldapService.getDisplay(tagLog.getEppn(), displayAttribut); 
			if(display != null) {
				return display;  
			}
		}
		return null;
	}

	public void setLocationFilter(String locationFilter) {
		this.locationFilter = locationFilter;
	}
}
