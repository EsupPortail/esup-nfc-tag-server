package org.esupportail.nfctag.beans;

import java.io.Serializable;
import java.util.List;

import org.esupportail.nfctag.service.api.TagUpdateApi;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.esupportail.nfctag.service.api.impl.TagUpdateNone;
import org.esupportail.nfctag.service.api.impl.TagWriteNone;

public class DesfireTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<DesfireApplication> applications;
	
	TagWriteApi tagWriteApi = new TagWriteNone();

	TagUpdateApi tagUpdateApi = new TagUpdateNone();
	
	Boolean formatBeforeWrite;
	
	public List<DesfireApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}

	public TagWriteApi getTagWriteApi() {
		return tagWriteApi;
	}

	public void setTagWriteApi(TagWriteApi tagWriteApi) {
		this.tagWriteApi = tagWriteApi;
	}

	public void setTagUpdateApi(TagUpdateApi tagUpdateApi) {
		this.tagUpdateApi = tagUpdateApi;
	}
	
	public TagUpdateApi getTagUpdateApi() {
		return tagUpdateApi;
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
