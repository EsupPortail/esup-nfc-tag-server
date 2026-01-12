package org.esupportail.nfctag.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.LdapQueryBuilder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class LdapGroupService implements GroupService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	LdapTemplate ldapTemplate;
	
	String groupSearchBase;
	
	String groupSearchFilter;
	
	String memberSearchFilter;
	
	String memberSearchBase;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	public void setMemberSearchBase(String memberSearchBase) {
		this.memberSearchBase = memberSearchBase;
	}

	public void setMemberSearchFilter(String memberSearchFilter) {
		this.memberSearchFilter = memberSearchFilter;
	}
	
	/* (non-Javadoc)
	 * @see org.esupportail.nfctag.security.GroupService#getGroupOfNamesForEppn(java.lang.String)
	 */
	@Override
	public List<String> getGroups(String eppn) {
		
		String username = eppn.replaceAll("@.*", "");	
		
		List<String> dns = ldapTemplate.search(LdapQueryBuilder.query().where("eduPersonPrincipalName").is(eppn),
				new AbstractContextMapper<String>() {
					@Override
					protected String doMapFromContext(DirContextOperations ctx) {
				        String dn = ctx.getNameInNamespace();
						return dn;
					}
			
		});
		
		List<String> groups = new ArrayList<String>();
		
		if(!dns.isEmpty()) {
			String userDn = dns.get(0);
			String formattedFilter = MessageFormat.format(groupSearchFilter, userDn, username);
			
			groups = ldapTemplate.search(
					groupSearchBase, formattedFilter, new AbstractContextMapper<String>() {
						@Override
						protected String doMapFromContext(DirContextOperations ctx) {
					        String dn = ctx.getNameInNamespace();
							return dn;
						}
					});
		} 
		
		log.debug(String.format("%s is in groups : %s", eppn, StringUtils.join(groups, ", ")));
		
		return groups;
		
	}

}

