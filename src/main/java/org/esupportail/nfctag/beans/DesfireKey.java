package org.esupportail.nfctag.beans;

import java.io.Serializable;

import org.esupportail.nfctag.service.desfire.DesfireKeyService;

public class DesfireKey implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String keyNo;
	
	private String key;
	
	private String keyVer;
	
	private DesfireKeyService desfireKeyService;
	
	public String getKeyNo() {
		return keyNo;
	}

	public void setKeyNo(String keyNo) {
		this.keyNo = keyNo;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKeyVer() {
		return keyVer;
	}

	public void setKeyVer(String keyVer) {
		this.keyVer = keyVer;
	}

	public DesfireKeyService getDesfireKeyService() {
		return desfireKeyService;
	}

	public void setDesfireKeyService(DesfireKeyService desfireKeyService) {
		this.desfireKeyService = desfireKeyService;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((keyNo == null) ? 0 : keyNo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DesfireKey other = (DesfireKey) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (keyNo == null) {
			if (other.keyNo != null)
				return false;
		} else if (!keyNo.equals(other.keyNo))
			return false;
		return true;
	}
		
}
