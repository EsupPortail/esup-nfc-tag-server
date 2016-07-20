package org.esupportail.nfctag.service.api;

import java.util.List;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;

public interface AppliExtApi {

	List<String> getLocations4Eppn(String eppn) throws EsupNfcTagException;
	
	void isTagable(TagLog tagLog) throws EsupNfcTagException;

	boolean validateTag(TagLog tagLog);
	
	boolean cancelTag(TagLog tagLog);
	
	String getDescription();
	
	String getHeader();
	
	String getBackgroundColor();
	
}