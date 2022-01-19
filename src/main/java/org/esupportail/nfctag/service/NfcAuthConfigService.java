/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.service;

import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

