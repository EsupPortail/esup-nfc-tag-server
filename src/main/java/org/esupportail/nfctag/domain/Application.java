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
package org.esupportail.nfctag.domain;
import javax.annotation.Resource;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.esupportail.nfctag.service.ApplisExtService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.TagIdCheckService;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findApplicationsByNameEquals" })
public class Application {

	@Transient
	@Resource
	ApplisExtService applisExtService;

	@Transient
	@Resource
	NfcAuthConfigService nfcAuthConfigService; 

	@Transient
	@Resource
	TagIdCheckService tagIdCheckService; 
	
    @NotNull
    private String name;

    @NotNull
    private String nfcConfig;

    @NotNull
    private String appliExt;

    @NotNull
    private String tagIdCheck;

    private String description;
    
    private boolean active = true;
    
    public String getNfcConfigDesc(){
    	NfcAuthConfig nfcAuthConfig = nfcAuthConfigService.get(this.nfcConfig);
    	if(nfcAuthConfig != null) {
    		return nfcAuthConfig.getDescription();
    	} else { 
    		return null;
    	}
    }
    
    public String getAppliExtDesc(){
    	AppliExtApi extApi = applisExtService.get(this.appliExt);
    	if(extApi != null) {
    		return extApi.getDescription();
    	} else { 
    		return null;
    	}
    }
    
    public String getTagIdCheckDesc(){
    	TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(this.tagIdCheck);
    	if(tagIdCheckApi != null) {
    		return tagIdCheckApi.getDescription();
    	} else { 
    		return null;
    	}		
    }
    
}
