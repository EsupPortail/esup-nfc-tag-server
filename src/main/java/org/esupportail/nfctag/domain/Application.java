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
    	return nfcAuthConfig.getDescription();
    }
    
    public String getAppliExtDesc(){
    	AppliExtApi extApi = applisExtService.get(this.appliExt);
    	return extApi.getDescription();
    }
    
    public String getTagIdCheckDesc(){
    	TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(this.tagIdCheck);
    	return tagIdCheckApi.getDescription();
    }
    
}
