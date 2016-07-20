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
