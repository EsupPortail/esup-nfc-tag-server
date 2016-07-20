package org.esupportail.nfctag.service.api.impl;

import java.util.List;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.LdapService;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TagIdCheckLdap implements TagIdCheckApi {
	
	public final static String CSN_SUFFIX_SWISS = "ISO15693";
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected List<LdapService> ldapServices;

	protected String desfireSuffix;
	
	protected String description;
	
	public void setDesfireSuffix(String desfireSuffix) {
		this.desfireSuffix = desfireSuffix;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public TagLog getTagLogFromTagId(TagType tagType, String tagId){
		TagLog tagLog = null;
		String cardUidType = null;
		switch (tagType) {
			case CSN :
				cardUidType = CSN_SUFFIX_SWISS;
				String csn = tagId;
				String csnRetourne = new String();
				for (int i = 1; i < csn.length(); i = i + 2) {
					csnRetourne = csnRetourne + csn.charAt(csn.length() - i - 1) + csn.charAt(csn.length() - i);
				}
				tagId = csnRetourne;				
				break;
			case DESFIRE :
				cardUidType = desfireSuffix;
				break;	
			default:
				throw new RuntimeException(tagType + " is not supported");
		}		
		for (LdapService ldapService : ldapServices) {
			if((tagLog = ldapService.getTagLogBySwissEduPersonCardUID(tagId, cardUidType)) != null) {
				break;
			}
		}
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
