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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.esupportail.nfctag.domain.ApplisExtApiForm;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplisExtService {

    private final Map<String, AppliExtApi> applisExtApi;

    @Autowired
    public ApplisExtService(Map<String, AppliExtApi> applisExtApi) {
        this.applisExtApi = applisExtApi;
    }

	public List<ApplisExtApiForm> getApplisExtApiForms() {
		List<String> applisExtKeys =  new ArrayList<String>(applisExtApi.keySet());
		Collections.sort(applisExtKeys);
		List<ApplisExtApiForm> extApiForms = new ArrayList<ApplisExtApiForm>();
		for(String appliExtKey : applisExtKeys) {
			AppliExtApi appliExtApi = this.applisExtApi.get(appliExtKey);
			ApplisExtApiForm extApiForm = new ApplisExtApiForm();
			extApiForm.setAppliExtKey(appliExtKey);
			extApiForm.setAppliExtDescription(appliExtApi.getDescription());	
			extApiForms.add(extApiForm);
		}
		return extApiForms;
	}
	
	public AppliExtApi get(String appliExtKey) {
		if(appliExtKey == null) return null;
		return this.applisExtApi.get(appliExtKey);
	}

	public Set<String> getApplisExtApiKeys() {
		return applisExtApi.keySet();
	}
	
}
