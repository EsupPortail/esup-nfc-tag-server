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

