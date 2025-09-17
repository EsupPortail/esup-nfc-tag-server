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

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.service.ApplicationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequestMapping("/manager/devices")
@Controller
public class DeviceController {
	
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	List<String> listSearchBy = Arrays.asList("numeroId", "eppnInit", "imei", "macAddress", "location");
	
	@Autowired
	private ApplicationsService applicationsService;

    @Resource
    private ApplicationDao applicationDao;

    @Resource
    private DeviceDao deviceDao;

    @PersistenceContext
    private EntityManager entityManager;

    @ModelAttribute("active")
    public String getActiveMenu() {
        return "devices";
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
    public String create(@Valid Device device, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, device);
            return "templates/manager/devices/update";
        }
        uiModel.asMap().clear();
        device.setCreateDate(new Date());
        deviceDao.persist(device);
        return "redirect:/manager/devices";
    }
    
    @RequestMapping(value = "/numeroid/{numeroId}", produces = "text/html")
    public String numeroId(@PathVariable("numeroId") String numeroId, Model uiModel) {
    	try{
    		Device device = deviceDao.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
            uiModel.addAttribute("device", device);
            uiModel.addAttribute("itemId", device.getId());
    	}catch(EmptyResultDataAccessException e){
    		log.debug("No device with id " + numeroId);
    	}
        return "templates/manager/devices/update";
    }
 
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(required = false) Application applicationFilter,
                       @RequestParam(required = false) String searchText,
                       @PageableDefault(size = 10, sort = "lastUseDate", direction = Sort.Direction.DESC) Pageable pageable,
                       Model uiModel) {
    	Page<Device> devices = deviceDao.findDeviceEntries(applicationFilter, searchText, pageable);
        uiModel.addAttribute("applications", applicationDao.findAllApplications());
        uiModel.addAttribute("status", TagLog.Status.values());
    	uiModel.addAttribute("devices", devices);
        uiModel.addAttribute("applicationFilter", applicationFilter);
        uiModel.addAttribute("searchText", searchText);
        addDateTimeFormatPatterns(uiModel);
        return "templates/manager/devices/list";
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new Device());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
        uiModel.addAttribute("eppn", eppn);
        return "templates/manager/devices/update";
    }
    
	@RequestMapping(value="/locationsJson", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody
	public List<String> selectedLocationForm(
			@RequestParam(required = true) String eppn,
			@RequestParam(required = true) Long applicationId
			) {
		List<String> json = new ArrayList<String>();
		try {
			List<Application> applications = applicationsService.getApplications4Eppn(eppn, false);
			if (applications.isEmpty()) {
				log.info(eppn + " don't have applications to manage");
				throw new AccessDeniedException(eppn + " don't have applications to manage");
			}
			for (Application application : applications) {
				if(application.getId().equals(applicationId)){
					for (String locationName : application.getLocations()) {
						json.add(locationName);
					}
				}
			}
		} catch (EsupNfcTagException e) {
			log.error("can't get locations", e);
		}
		return json;
	}

	@RequestMapping(value="/getValidateWo", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody
	public Boolean getValidateWo(@RequestParam(required = true) Long applicationId) {
		Application application = applicationDao.findApplication(applicationId);
		return application.getValidateAuthWoConfirmationDefault();
	}

    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, deviceDao.findDevice(id));
        return "templates/manager/devices/update";
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Device device, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, device);
            return "templates/manager/devices/update";
        }
        uiModel.asMap().clear();
        deviceDao.merge(device);
        return "redirect:/manager/devices";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        deviceDao.remove(id);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/manager/devices";
    }

    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("device_createdate_date_format",  "dd/MM/yyyy HH:mm:ss");
        uiModel.addAttribute("device_lastusedate_date_format", "dd/MM/yyyy HH:mm:ss");
    }

    void populateEditForm(Model uiModel, Device device) {
        uiModel.addAttribute("device", device);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("applications", applicationDao.findAllApplications());
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
