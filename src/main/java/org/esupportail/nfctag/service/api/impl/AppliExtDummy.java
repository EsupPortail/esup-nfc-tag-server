package org.esupportail.nfctag.service.api.impl;

import java.util.Arrays;
import java.util.List;

import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.api.AppliExtApi;

public class AppliExtDummy implements AppliExtApi {

	@Override
	public String getDescription() {
		return "Dummy !";
	};

	@Override
	public String getBackgroundColor() {
		return "black";
	};

	@Override
	public String getHeader() {
		return "";
	};
	
	@Override
	public List<String> getLocations4Eppn(String eppn) {
		return Arrays.asList("Dummy Location");
	}

	@Override
	public void isTagable(TagLog tagLog) throws EsupNfcTagException {
		
	}
	
	@Override
	public boolean validateTag(TagLog tagLog){
		return true;
	}
	
	@Override
	public boolean cancelTag(TagLog tagLog){
		return true;
	}

}
