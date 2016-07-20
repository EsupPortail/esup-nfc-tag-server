package org.esupportail.nfctag.web.manager;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.nfctag.domain.ApplisExtApiForm;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.TagIdCheckApiForm;
import org.esupportail.nfctag.service.ApplisExtService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.TagIdCheckService;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/manager/applications")
@Controller
@RooWebScaffold(path = "manager/applications", formBackingObject = Application.class)
public class ApplicationController {
	
	@Resource
	ApplisExtService applisExtService;
	
	@Resource
	TagIdCheckService tagIdCheckService;
	
	@Resource
	NfcAuthConfigService nfcAuthConfigService;
	
	@ModelAttribute("extApis")
	public List<ApplisExtApiForm> getApplisExtApiForms() {
		return applisExtService.getApplisExtApiForms();
	}
	
	@ModelAttribute("tagIdChecks")
	public List<TagIdCheckApiForm> getTagIdCheckApiForms() {
		return tagIdCheckService.getTagIdCheckApiForms();
	}
	
	@ModelAttribute("nfcAuthConfigs")
	public List<NfcAuthConfig> getNfcAuthConfigs() {
		return nfcAuthConfigService.getNfcAuthConfigs();
	}
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Application application, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, application);
            return "manager/applications/create";
        }
        uiModel.asMap().clear();
        application.setActive(true);
        application.persist();
        return "redirect:/manager/applications/";
    }
    
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Application application, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, application);
            return "manager/applications/update";
        }
        uiModel.asMap().clear();
        Application applicationUpdate = Application.findApplication(application.getId());
        applicationUpdate.setTagIdCheck(application.getTagIdCheck());
        applicationUpdate.setAppliExt(application.getAppliExt());
        applicationUpdate.setNfcConfig(application.getNfcConfig());
        applicationUpdate.setActive(application.isActive());
        applicationUpdate.merge();
        return "redirect:/manager/applications/";
    }
}
