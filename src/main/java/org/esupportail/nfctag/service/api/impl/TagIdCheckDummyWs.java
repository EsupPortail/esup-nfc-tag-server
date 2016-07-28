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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.domain.TagLog.Status;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagIdCheckDummyWs implements TagIdCheckApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
    
    protected String description;
    
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	List<List<String>> daltons = new ArrayList<List<String>>();
	
	@Override
	public TagLog getTagLogFromTagId(TagType tagType, String tagId) throws EsupNfcTagException {
		
		String desfireId = null;
		
		int numDalton = 0;
		
		daltons.add(Arrays.asList("Joe", "Dalton"));
		daltons.add(Arrays.asList("Jack", "Dalton"));
		daltons.add(Arrays.asList("William", "Dalton"));
		daltons.add(Arrays.asList("Averell", "Dalton"));
		
		switch (tagType) {
			case CSN :
				String csn = tagId;
				String csnRetourne = new String();
				for (int i = 1; i < csn.length(); i = i + 2) {
					csnRetourne = csnRetourne + csn.charAt(csn.length() - i - 1) + csn.charAt(csn.length() - i);
				}
				tagId = csnRetourne;
				numDalton = Integer.parseInt(tagId.substring(0, 4), 16) % 4;
				log.info("Check CSN : " + csnRetourne);
				break;
			case DESFIRE :
				desfireId = tagId;
				log.info("Check Desfire : " + desfireId);
				break;
			default:
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_typenotsupported, tagType.toString());
		}		
		
		
		TagLog tagLog = new TagLog();
		tagLog.setCsn(tagId);
		tagLog.setFirstname(daltons.get(numDalton).get(0));
		tagLog.setLastname(daltons.get(numDalton).get(1));
		tagLog.setStatus(Status.valid);
		tagLog.setAuthDate(new Date());
		tagLog.setEppn(daltons.get(numDalton).get(1)+"@univ-ville.fr");
		tagLog.setLocation("Dummy Location");

		return tagLog;	
	}
	
	@Override
	public Boolean supportTagType(TagType tagType) {
		switch (tagType) {
			case CSN :
				return true;
			case DESFIRE :
				return true;	
			default:
				return false;
		}
	}
	
}
