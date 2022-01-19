package org.esupportail.nfctag.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;

public class ShibRequestHeaderAuthenticationFilter extends RequestHeaderAuthenticationFilter {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private String credentialsRequestHeader4thisClass;
	
	/* 
	 * Surcharge de la méthode initiale : si pas d'attributs correspondant à credentialsRequestHeader (shib) ; on continue  :
	 * 	credentials ldap suffisent (et pas de credentials du tout aussi ...). 
	 * 
	 * @see org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter#getPreAuthenticatedCredentials(javax.servlet.http.HttpServletRequest)
	 */
	@Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		String credentials = null;
        if (credentialsRequestHeader4thisClass != null) {
        	credentials = request.getHeader(credentialsRequestHeader4thisClass);
        }
        if(credentials == null) {
        	return "N/A"; 
        } else {
        	return credentials;
        }
    }
	
    public void setCredentialsRequestHeader(String credentialsRequestHeader) {
        super.setCredentialsRequestHeader(credentialsRequestHeader);
        this.credentialsRequestHeader4thisClass = credentialsRequestHeader;
    }

}
