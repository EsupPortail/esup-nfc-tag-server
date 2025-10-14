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
package org.esupportail.nfctag.web.manager;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.ApplisExtApiForm;
import org.esupportail.nfctag.domain.TagIdCheckApiForm;
import org.esupportail.nfctag.service.ApplisExtService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.TagIdCheckService;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.util.List;

@RequestMapping("/manager/applications")
@Controller
public class ApplicationController {
	
	@Resource
	ApplisExtService applisExtService;
	
	@Resource
	TagIdCheckService tagIdCheckService;
	
	@Resource
	NfcAuthConfigService nfcAuthConfigService;

    @Resource
    private ApplicationDao applicationDao;

    @Resource
    private DeviceDao deviceDao;

    @ModelAttribute("active")
    public String getActiveMenu() {
        return "applications";
    }

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

    /*
     *  Empty strings from web forms are nullified
     */
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Application application, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, application);
            return "templates/manager/applications/create";
        }
        uiModel.asMap().clear();
        application.setActive(true);
        applicationDao.persist(application);
        return "redirect:/manager/applications";
    }

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new Application());
        return "templates/manager/applications/update";
    }

    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "id") Pageable pageable,
                       Model uiModel) {
        uiModel.addAttribute("applications", applicationDao.findAllApplications(pageable));
        return "templates/manager/applications/list";
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Application application, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, application);
            return "templates/manager/applications/update";
        }
        uiModel.asMap().clear();
        applicationDao.merge(application);
        return "redirect:/manager/applications";
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        Application application = applicationDao.findApplication(id);
        populateEditForm(uiModel, application);
        uiModel.addAttribute("devices", deviceDao.findDevicesByApplication(application));
        return "templates/manager/applications/update";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        applicationDao.remove(id);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/manager/applications";
    }

    void populateEditForm(Model uiModel, Application application) {
        uiModel.addAttribute("application", application);
        uiModel.addAttribute("app", application);
    }

    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}
