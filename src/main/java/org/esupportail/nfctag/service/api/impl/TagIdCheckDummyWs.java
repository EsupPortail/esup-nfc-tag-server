package org.esupportail.nfctag.service.api.impl;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagIdCheckDummyWs implements TagIdCheckApi {

	private final Logger log = LoggerFactory.getLogger(getClass());
    
    protected String description;
    
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public TagLog getTagLogFromTagId(TagType tagType, String tagId) throws EsupNfcTagException {
		
		String desfireId = null;		
		
		switch (tagType) {
			case CSN :
				String csn = tagId;
				String csnRetourne = new String();
				for (int i = 1; i < csn.length(); i = i + 2) {
					csnRetourne = csnRetourne + csn.charAt(csn.length() - i - 1) + csn.charAt(csn.length() - i);
				}
				tagId = csnRetourne;
				log.info("Check CSN : " + csnRetourne);
				break;
			case DESFIRE :
				desfireId = tagId;
				log.info("Check Desfire : " + desfireId);
				break;
			default:
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_typenotsupported, tagType.toString());
		}		
		
		TagLog tagLog = new TagLog();
		tagLog.setCsn(tagId);
		tagLog.setDesfireId(desfireId);
		tagLog.setLastname("Joe");
		tagLog.setFirstname("Dalton");

		return tagLog;	
	}
	
	@Override
	public Boolean supportTagType(TagType tagType) {
		switch (tagType) {
			case CSN :
				return true;
			case DESFIRE :
				return true;	
			default:
				return false;
		}
	}
	
}
