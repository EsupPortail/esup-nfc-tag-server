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

import java.text.MessageFormat;
import java.util.List;

import javax.naming.directory.Attributes;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class LdapService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private LdapTemplate ldapTemplate;


	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public boolean isTagable(String eppn, String isTagableFilter) {
		try{
			List<String> result = ldapTemplate.search("", "(&(eduPersonPrincipalName=" + eppn + ")("+isTagableFilter+"))", new ContextMapper<String>() {
					@Override
					public String mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
				        String eppn = searchResultContext.getStringAttribute("eduPersonPrincipalName");
						return eppn;
					}
				});
			if(!result.isEmpty()) {
				log.info("Ldap entry for " + eppn + " this filter : " + isTagableFilter + "isTagable = true");
				return true;
			} else {
				log.warn("NO ldap entry for " + eppn + " this filter : " + isTagableFilter + " isTagable = false");
				return false;
			}
		} catch (Exception e){
			log.warn("ldap request error for " + eppn + " with istagable filter" + isTagableFilter, e);
			return false;
		}
	}
	
	public boolean locationRightCheck(String eppnInit, String locationFilter) {
		try{
			List<String> result = ldapTemplate.search("", "(&(eduPersonPrincipalName=" + eppnInit + ")("+locationFilter+"))", new ContextMapper<String>() {
					@Override
					public String mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
				        String eppn = searchResultContext.getStringAttribute("eduPersonPrincipalName");
						return eppn;
					}
				});
			if(!result.isEmpty()) {
				log.info("Ldap entry for " + eppnInit + " this filter : " + locationFilter + " location = access");
				return true;
			} else {
				log.warn("NO ldap entry for " + eppnInit + " this filter : " + locationFilter + " location = denied");
				return false;
			}
		} catch (Exception e){
			log.warn("ldap request error for " + eppnInit + " with istagable filter" + locationFilter, e);
			return false;
		}
	}
	
	public String getDisplay(String eppn, String displayAttribut) {
		try {
			List<String> result = ldapTemplate.search("", "(&(eduPersonPrincipalName=" + eppn + "))", new ContextMapper<String>() {
					@Override
					public String mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
				        String result = searchResultContext.getStringAttribute(displayAttribut);
						return result;
					}
				});
			if(!result.isEmpty()) {
				log.info("Ldap entry for " + eppn + ", " + displayAttribut + " is " + result.get(0));
				return result.get(0);
			} else {
				log.warn("NO ldap entry for " + eppn);
				return null;
			}
		} catch (Exception e){
			log.warn("ldap request error for " + eppn + " with display Attribut " + displayAttribut, e);
			return null;
		}
	}
	
	public TagLog getTagLogByCardUID(String cardUID, String searchFilter, TagType tagType) {
		
		String formattedFilter = MessageFormat.format(searchFilter, new String[] {cardUID});
		
		List<TagLog> tagLogs = ldapTemplate.search("", "(&(objectclass=person)("+formattedFilter+"))",
				new TagLogLdapAttributMapper(tagType, cardUID));
		if(!tagLogs.isEmpty()) {
			log.info("Got ldap entry from : " + ((LdapContextSource)ldapTemplate.getContextSource()).getUrls() + "for this filter : " + formattedFilter);
			return tagLogs.get(0);
		} else {
			log.warn("Got NO ldap entry from : " + ((LdapContextSource)ldapTemplate.getContextSource()).getUrls() + "for this filter : " + formattedFilter);
			return null;
		}
	}

	public class TagLogLdapAttributMapper implements AttributesMapper<TagLog> {
		
		private TagType tagType;
		private String cardUID;
		
		public TagLogLdapAttributMapper(TagType tagType, String cardUID) {
			super();
			this.tagType = tagType;
			this.cardUID = cardUID;
			
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
			switch (tagType) {
				case CSN:
					tagLog.setCsn(cardUID);
					break;
	
				case DESFIRE:
					tagLog.setDesfireId(cardUID);
					break;
			}
			return tagLog;
		}
		
		
	}
}
