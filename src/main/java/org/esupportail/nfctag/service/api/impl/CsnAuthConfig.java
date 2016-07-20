package org.esupportail.nfctag.service.api.impl;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class CsnAuthConfig extends NfcAuthConfig {

	@Override
	public AuthType getAuthType() {
		return AuthType.CSN;
	}
	
    
}
