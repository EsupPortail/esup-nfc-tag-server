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

import java.util.List;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.LdapService;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TagIdCheckLdap implements TagIdCheckApi {
	
	public final static String CSN_SUFFIX_SWISS = "ISO15693";
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected List<LdapService> ldapServices;

	protected String desfireSuffix;
	
	protected String description;
	
	public void setDesfireSuffix(String desfireSuffix) {
		this.desfireSuffix = desfireSuffix;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public TagLog getTagLogFromTagId(TagType tagType, String tagId, String appName){
		TagLog tagLog = null;
		String cardUidType = null;
		switch (tagType) {
			case CSN :
				cardUidType = CSN_SUFFIX_SWISS;		
				break;
			case DESFIRE :
				cardUidType = desfireSuffix;
				break;	
			default:
				throw new RuntimeException(tagType + " is not supported");
		}		
		for (LdapService ldapService : ldapServices) {
			if((tagLog = ldapService.getTagLogBySwissEduPersonCardUID(tagId, cardUidType)) != null) {
				break;
			}
		}
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
