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
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppliExtDummy implements AppliExtApi {

	private final Logger log = LoggerFactory.getLogger(getClass());

	List<String> locationsNames = new ArrayList<String>();

	private String eppnFilter = ".*";
	
	protected String description;
	
	public void setLocationsNames(List<String> locationsNames) {
		this.locationsNames = locationsNames;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getDescription() {
		if(description != null) {
			return description;
		}
		return "Dummy !";
	};

	@Override
	public String getBackgroundColor() {
		return "black";
	};

	@Override
	public String getHeader() {
		return "";
	};
	
	@Override
	public List<String> getLocations4Eppn(String eppn) {
		if(eppn.matches(eppnFilter)) {
			if(locationsNames.size() > 0 ){
				return locationsNames;
			}
			return Arrays.asList("Dummy Location");
		}
		log.error(eppn + " not autorized to access this app");
		return null;
	}

	@Override
	public void isTagable(TagLog tagLog) throws EsupNfcTagException {
		
	}
	
	@Override
	public boolean validateTag(TagLog tagLog){
		return true;
	}
	
	@Override
	public boolean cancelTag(TagLog tagLog){
		return true;
	}

	@Override
	public boolean isDisplay(){
		return true;
	}
	
	@Override
	public String getDisplay(TagLog tagLog){
		return tagLog.getCsn() + "<br/>" + tagLog.getDesfireId();
	}
	
}
