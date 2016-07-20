package org.esupportail.nfctag.service.desfire;

import java.io.Serializable;

import org.esupportail.nfctag.service.api.impl.DesfireAuthConfig;

public class DesfireAuthSession implements Serializable {

	private static final long serialVersionUID = 1L;
	
	DesfireAuthConfig desfireAuthConfig;

	private String rndBPrimEnc;
	
	private String rndA;
	
	private String rndBdecrypt;
	
	private String sessionKey;
	
	public String getRndBPrimEnc() {
		return rndBPrimEnc;
	}
	public void setRndBPrimEnc(String rndBPrimEnc) {
		this.rndBPrimEnc = rndBPrimEnc;
	}
	public String getRndA() {
		return rndA;
	}
	public void setRndA(String rndA) {
		this.rndA = rndA;
	}
	public String getRndBdecrypt() {
		return rndBdecrypt;
	}
	public void setRndBdecrypt(String rndBdecrypt) {
		this.rndBdecrypt = rndBdecrypt;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public DesfireAuthConfig getDesfireAuthConfig() {
		return desfireAuthConfig;
	}
	public void setDesfireAuthConfig(DesfireAuthConfig desfireAuthConfig) {
		this.desfireAuthConfig = desfireAuthConfig;
	}
}
