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

public class TagIdCheckLdap implements TagIdCheckApi {
	
	protected List<LdapService> ldapServices;

	protected String searchFilter;
	
	protected String description;
	
	public void setLdapServices(List<LdapService> ldapServices) {
		this.ldapServices = ldapServices;
	}

	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
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
		for (LdapService ldapService : ldapServices) {
			if((tagLog = ldapService.getTagLogByCardUID(tagId, searchFilter, tagType)) != null) {
				break;
			}
		}
		return tagLog;	
	}
}
