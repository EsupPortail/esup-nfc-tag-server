package org.esupportail.nfctag.service.api;

public abstract class NfcAuthConfig {
	
	String id;
	
	String description;
	
	public enum AuthType{
		CSN, DESFIRE
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public abstract AuthType getAuthType();	
}
