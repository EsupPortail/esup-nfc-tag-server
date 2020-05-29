package org.esupportail.nfctag.beans;

import java.io.Serializable;

import org.esupportail.nfctag.service.api.TagWriteApi;
import org.esupportail.nfctag.service.api.impl.TagWriteNone;
import org.esupportail.nfctag.service.desfire.DesfireUtils;

public class DesfireFile implements Serializable {

	private static final long serialVersionUID = 1L;

	String fileNumber;
	
	String isoId;
	
	String communicationSettings;
	
	String accessRights;
	
	String fileSize;
	
	TagWriteApi tagWriteApi = new TagWriteNone();
	
	public String getWriteFilePayload(boolean iso) {
		// accessRights is setting after write command here
		// return fileNumber + communicationSettings + accessRights + fileSize;
		if(iso) {
			assert(isoId!=null && isoId.length()==4);
			String lsbIsoId = DesfireUtils.swapPairs(DesfireUtils.hexStringToByteArray(isoId));
			return fileNumber + lsbIsoId + communicationSettings + "EEEE";
		} else {
			return fileNumber + communicationSettings + "EEEE";
		}
	}

	public String getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(String fileNumber) {
		this.fileNumber = fileNumber;
	}
	
	public String getIsoId() {
		return isoId;
	}

	public void setIsoId(String isoId) {
		this.isoId = isoId;
	}

	public String getCommunicationSettings() {
		return communicationSettings;
	}

	public void setCommunicationSettings(String communicationSettings) {
		this.communicationSettings = communicationSettings;
	}

	public String getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	
	public TagWriteApi getTagWriteApi() {
		return tagWriteApi;
	}

	public void setTagWriteApi(TagWriteApi tagWriteApi) {
		this.tagWriteApi = tagWriteApi;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessRights == null) ? 0 : accessRights.hashCode());
		result = prime * result + ((communicationSettings == null) ? 0 : communicationSettings.hashCode());
		result = prime * result + ((fileNumber == null) ? 0 : fileNumber.hashCode());
		result = prime * result + ((fileSize == null) ? 0 : fileSize.hashCode());
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
		DesfireFile other = (DesfireFile) obj;
		if (accessRights == null) {
			if (other.accessRights != null)
				return false;
		} else if (!accessRights.equals(other.accessRights))
			return false;
		if (communicationSettings == null) {
			if (other.communicationSettings != null)
				return false;
		} else if (!communicationSettings.equals(other.communicationSettings))
			return false;
		if (fileNumber == null) {
			if (other.fileNumber != null)
				return false;
		} else if (!fileNumber.equals(other.fileNumber))
			return false;
		if (fileSize == null) {
			if (other.fileSize != null)
				return false;
		} else if (!fileSize.equals(other.fileSize))
			return false;
		return true;
	}
	
}
