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
package org.esupportail.nfctag.service;

import java.util.Collections;
import java.util.List;

import javax.naming.directory.Attributes;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.api.impl.TagIdCheckLdap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;

public class LdapService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public TagLog getTagLogBySwissEduPersonCardUID(String cardUID, String cardUidType) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		filter.and(new LikeFilter("swissEduPersonCardUID", cardUID + "@" + cardUidType));
		List<TagLog> tagLogs = ldapTemplate.search("", filter.encode(),
				new TagLogLdapAttributMapper(cardUidType));
		if(!tagLogs.isEmpty()) {
			log.info("Got ldap entry from : " + ((LdapContextSource)ldapTemplate.getContextSource()).getUrls() + "for this swissEduPersonCardUID " + cardUID + "@" + cardUidType);
			return tagLogs.get(0);
		} else {
			log.warn("Got NO ldap entry from : " + ((LdapContextSource)ldapTemplate.getContextSource()).getUrls() + "for this swissEduPersonCardUID " + cardUID + "@" + cardUidType);
			return null;
		}
	}

	
	public class TagLogLdapAttributMapper implements AttributesMapper<TagLog> {
		
		private String cardUidType;
		
		public TagLogLdapAttributMapper(String cardUidType) {
			super();
		}

		public TagLog mapFromAttributes(Attributes attrs) throws javax.naming.NamingException {
			
			TagLog tagLog = new TagLog();
			
			if (attrs.get("eduPersonPrincipalName") != null) {
				tagLog.setEppn(attrs.get("eduPersonPrincipalName").get().toString());
			}
			if (attrs.get("givenName") != null) {
				tagLog.setFirstname(attrs.get("givenName").get().toString());
			}
			if (attrs.get("sn") != null) {
				tagLog.setLastname(attrs.get("sn").get().toString());
			}
			if (attrs.get("eduPersonPrincipalName") != null) {
				tagLog.setEppn(attrs.get("eduPersonPrincipalName").get().toString());
			}
			if (attrs.get("swissEduPersonCardUID") != null) {
				List<String> swissEduPersonCardUIDs = (List<String>) Collections.list(attrs.get("swissEduPersonCardUID").getAll());
				for(String swissEduPersonCardUID: swissEduPersonCardUIDs) {
					if(swissEduPersonCardUID.endsWith("@" + cardUidType)) {
						String cardUid = swissEduPersonCardUID.replaceAll("@" + cardUidType, "");
						if(TagIdCheckLdap.CSN_SUFFIX_SWISS.equals(cardUidType)) {
							tagLog.setCsn(cardUid);
						} else {
							tagLog.setDesfireId(cardUid);
						}	
						break;
					}
				}
			}
			return tagLog;
		}
		
		
	}
}
