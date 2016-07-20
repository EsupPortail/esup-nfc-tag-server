package org.esupportail.nfctag.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NfcAuthConfigService {

    private final Map<String, NfcAuthConfig> nfcAuthConfigs;

    @Autowired
    public NfcAuthConfigService(Map<String, NfcAuthConfig> nfcAuthConfigs) {
    	super();
        this.nfcAuthConfigs = nfcAuthConfigs;
        for(String key : nfcAuthConfigs.keySet()) {
        	nfcAuthConfigs.get(key).setId(key);
        }
    }

	public List<NfcAuthConfig> getNfcAuthConfigs() {
		List<NfcAuthConfig> nfcAuthConfigsValues =  new ArrayList<NfcAuthConfig>(nfcAuthConfigs.values());
		Collections.sort(nfcAuthConfigsValues, (NfcAuthConfig c1, NfcAuthConfig c2) -> c1.getId().compareTo(c2.getId()));
		return nfcAuthConfigsValues;
	}
	
	public NfcAuthConfig get(String nfcAuthConfigKey) {
		if(nfcAuthConfigKey == null) return null;
		return this.nfcAuthConfigs.get(nfcAuthConfigKey);
	}
}

