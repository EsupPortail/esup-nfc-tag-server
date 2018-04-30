package org.esupportail.nfctag.security;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

public class LdapGroupService implements GroupService {
	
	LdapTemplate ldapTemplate;
	
	String groupSearchBase;
	
	String groupSearchFilter;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	/* (non-Javadoc)
	 * @see org.esupportail.nfctag.security.GroupService#getGroupOfNamesForEppn(java.lang.String)
	 */
	@Override
	public List<String> getGroups(String eppn) {
		
		String username = eppn.replaceAll("@.*", "");	
		
		List<String> dns = ldapTemplate.search(LdapQueryBuilder.query().where("eduPersonPrincipalName").is(eppn),
				new ContextMapper<String>() {

					@Override
					public String mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
				        String dn = searchResultContext.getNameInNamespace();
						return dn;
					}
			
		});
		
		List<String> groups = new ArrayList<String>();
		
		if(!dns.isEmpty()) {
			String userDn = dns.get(0);
			String formattedFilter = MessageFormat.format(groupSearchFilter, new String[] { userDn, username });
			
			groups = ldapTemplate.search(
					groupSearchBase, formattedFilter,new ContextMapper<String>() {
	
						@Override
						public String mapFromContext(Object ctx) throws NamingException {
							DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
					        String dn = searchResultContext.getNameInNamespace();
							return dn;
						}
					});
		} 
		
		return groups;
		
	}

}

