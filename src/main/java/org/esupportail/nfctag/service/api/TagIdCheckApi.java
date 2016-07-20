package org.esupportail.nfctag.service.api;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;

public interface TagIdCheckApi {
	
	public enum TagType {CSN, DESFIRE};

	TagLog getTagLogFromTagId(TagType tagType, String tagId) throws EsupNfcTagException;
	
	Boolean supportTagType(TagType tagType);
	
	String getDescription();

}
