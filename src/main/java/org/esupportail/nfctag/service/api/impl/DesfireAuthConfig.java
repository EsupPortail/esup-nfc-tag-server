package org.esupportail.nfctag.service.api.impl;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class DesfireAuthConfig extends NfcAuthConfig {
	
	private String name;
	
	private String desfireKey;
	
	private String desfireKeyNumber;
	
	private String desfireAppId;
	
	private String readFileCommand;
	
	@Override
	public AuthType getAuthType() {
		return AuthType.DESFIRE;
	}
    
}
