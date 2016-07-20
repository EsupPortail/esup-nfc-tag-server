package org.esupportail.nfctag.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.esupportail.nfctag.domain.TagIdCheckApiForm;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagIdCheckService {

    private final Map<String, TagIdCheckApi> tagIdsChecksApi;

    @Autowired
    public TagIdCheckService(Map<String, TagIdCheckApi> tagIdsChecksApi) {
        this.tagIdsChecksApi = tagIdsChecksApi;
    }

	public List<TagIdCheckApiForm> getTagIdCheckApiForms() {
		List<String> tagIdCheckKeys =  new ArrayList<String>(tagIdsChecksApi.keySet());
		Collections.sort(tagIdCheckKeys);
		List<TagIdCheckApiForm> tagIdCheckApiForms = new ArrayList<TagIdCheckApiForm>();
		for(String tagIdCheckKey : tagIdCheckKeys) {
			TagIdCheckApi tagIdCheckApi = this.tagIdsChecksApi.get(tagIdCheckKey);
			TagIdCheckApiForm extApiForm = new TagIdCheckApiForm();
			extApiForm.setTagIdCheckKey(tagIdCheckKey);
			extApiForm.setTagIdCheckDescription(tagIdCheckApi.getDescription());	
			tagIdCheckApiForms.add(extApiForm);
		}
		return tagIdCheckApiForms;
	}
	
	public TagIdCheckApi get(String tagIdCheckKey) {
		if(tagIdCheckKey == null) return null;
		return this.tagIdsChecksApi.get(tagIdCheckKey);
	}
}

