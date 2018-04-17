package org.esupportail.nfctag.beans;

import java.io.Serializable;
import java.util.List;

import org.esupportail.nfctag.service.api.TagUpdateApi;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.esupportail.nfctag.service.api.impl.TagUpdateNone;
import org.esupportail.nfctag.service.api.impl.TagWriteNone;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.KeyType;

public class DesfireTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<DesfireApplication> applications;
	
	Boolean formatBeforeWrite = false;
	
	/**
	 * PICC master key == key 0
	 */
	private String keyStart;
	
	private String keyVersionStart;

	/**
	 * keyType : AES, DES
	 */
	private KeyType keyTypeStart;

	private String keyFinish;
	
	private String keyVersionFinish;

	/**
	 * keyType : AES, DES
	 */
	private KeyType keyTypeFinish;
	
	public String getKeyStart() {
		return keyStart;
	}

	public void setKeyStart(String keyStart) {
		this.keyStart = keyStart;
	}

	public String getKeyVersionStart() {
		return keyVersionStart;
	}

	public void setKeyVersionStart(String keyVersionStart) {
		this.keyVersionStart = keyVersionStart;
	}

	public KeyType getKeyTypeStart() {
		return keyTypeStart;
	}

	public void setKeyTypeStart(KeyType keyTypeStart) {
		this.keyTypeStart = keyTypeStart;
	}

	public String getKeyFinish() {
		return keyFinish;
	}

	public void setKeyFinish(String keyFinish) {
		this.keyFinish = keyFinish;
	}

	public String getKeyVersionFinish() {
		return keyVersionFinish;
	}

	public void setKeyVersionFinish(String keyVersionFinish) {
		this.keyVersionFinish = keyVersionFinish;
	}

	public KeyType getKeyTypeFinish() {
		return keyTypeFinish;
	}

	public void setKeyTypeFinish(KeyType keyTypeFinish) {
		this.keyTypeFinish = keyTypeFinish;
	}

	public List<DesfireApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}
	
	public Boolean getFormatBeforeWrite() {
		return formatBeforeWrite;
	}

	public void setFormatBeforeWrite(Boolean formatBeforeWrite) {
		this.formatBeforeWrite = formatBeforeWrite;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applications == null) ? 0 : applications.hashCode());
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
		DesfireTag other = (DesfireTag) obj;
		if (applications == null) {
			if (other.applications != null)
				return false;
		} else if (!applications.equals(other.applications))
			return false;
		return true;
	}
	
}
