package org.esupportail.nfctag.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class ShibAuthenticatedUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	
	protected Map<String, String> mappingGroupesRoles;
	
	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		this.mappingGroupesRoles = mappingGroupesRoles;
	}
	
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws AuthenticationException {
		List<GrantedAuthorityImpl> authorities = new ArrayList<GrantedAuthorityImpl>();
		String credentials = (String)token.getCredentials();
		for(String credential : StringUtils.split(credentials, ";")) {
			if(mappingGroupesRoles != null && mappingGroupesRoles.containsKey(credential)) 
				authorities.add(new GrantedAuthorityImpl(mappingGroupesRoles.get(credential)));
		}
		return createUserDetails(token, authorities);
	}

	protected UserDetails createUserDetails(Authentication token, Collection<? extends GrantedAuthority> authorities) {
		return new User(token.getName(), "N/A", true, true, true, true, authorities);
	}
}
