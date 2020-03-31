package org.esupportail.nfctag.security;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

public class LdapFilterGroupService implements GroupService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	LdapTemplate ldapTemplate;
	
	Map<String, String> ldapFiltersGroups;
	
	String memberSearchFilter = "(&(eduPersonPrincipalName={0})({1}))";
	
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setLdapFiltersGroups(Map<String, String> ldapFiltersGroups) {
		this.ldapFiltersGroups = ldapFiltersGroups;
	}

	public void setMemberSearchFilter(String memberSearchFilter) {
		this.memberSearchFilter = memberSearchFilter;
	}

	@Override
	public List<String> getGroups(String eppn) {
		
		List<String> groups = new ArrayList<String>();
		
		for(String ldapFilter: ldapFiltersGroups.keySet()) {
			
			String harcodedFilter = MessageFormat.format(memberSearchFilter, new String[] {eppn, ldapFilter});
			
			List<String> dns = ldapTemplate.search(LdapQueryBuilder.query().filter(harcodedFilter),
					new ContextMapper<String>() {
	
						@Override
						public String mapFromContext(Object ctx) throws NamingException {
							DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
					        String dn = searchResultContext.getNameInNamespace();
							return dn;
						}
				
			});
			
			if(!dns.isEmpty()) {
				log.debug(String.format("%s match with ldap filter %s", eppn, ldapFilter));
				groups.add(ldapFiltersGroups.get(ldapFilter));
				log.debug(String.format("%s -> %s", eppn, ldapFiltersGroups.get(ldapFilter)));
			} else if(log.isTraceEnabled()) {
				log.trace(String.format("%s does not match with ldap filter %s", eppn, ldapFilter));
			}
		
		}
		
		return groups;
		
	}
	
}

