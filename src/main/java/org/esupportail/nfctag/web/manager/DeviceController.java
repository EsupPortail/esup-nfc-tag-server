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
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/devices")
@Controller
@RooWebScaffold(path = "manager/devices", formBackingObject = Device.class)
public class DeviceController {
	
	List<String> listSearchBy = Arrays.asList("numeroId", "eppnInit", "imei", "macAddress", "location");
	
    @RequestMapping(value = "/numeroid/{numeroId}", produces = "text/html")
    public String numeroId(@PathVariable("numeroId") String numeroId, Model uiModel) {
    	Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
        uiModel.addAttribute("device", device);
        uiModel.addAttribute("itemId", device.getId());
        return "manager/devices/show";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Device device, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, device);
            return "manager/devices/update";
        }
        uiModel.asMap().clear();
        device.merge();
        return "redirect:/manager/devices/";
    }
 
    @RequestMapping(produces = "text/html")
    public String list(
    		@RequestParam(value = "applicationFilter", required = false) Long applicationFilter,
    		@RequestParam(value = "searchBySelected", required = false) String searchBySelected,
    		@RequestParam(value = "searchString", required = false) String searchString,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

    	if (applicationFilter!=null) {
    		Application application = Application.findApplication(applicationFilter);
    		uiModel.addAttribute("devices", Device.findDevicesByApplicationEquals(application).getResultList());
        	uiModel.addAttribute("application", application);
    	}else if ("eppnInit".equals(searchBySelected)) {
    		uiModel.addAttribute("devices", Device.findDevicesByEppnInitLike(searchString).getResultList());
    	}else if ("numeroId".equals(searchBySelected)) {
    		uiModel.addAttribute("devices", Device.findDevicesByNumeroIdEquals(searchString).getResultList());
    	}else if ("imei".equals(searchBySelected)) {
    		uiModel.addAttribute("devices", Device.findDevicesByImeiEquals(searchString).getResultList());
    	} else if ("macAddress".equals(searchBySelected)) {
    		uiModel.addAttribute("devices", Device.findDevicesByMacAddressEquals(searchString).getResultList());
    	} else if ("location".equals(searchBySelected)) {
    		uiModel.addAttribute("devices", Device.findDevicesByLocationEquals(searchString).getResultList());
    	}else {
		    if (page != null || size != null) {
		        int sizeNo = size == null ? 10 : size.intValue();
		        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
		        uiModel.addAttribute("devices", Device.findDeviceEntries(firstResult, sizeNo, sortFieldName, sortOrder));
		        float nrOfPages = (float) Device.countDevices() / sizeNo;
		        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
		    } else {
		        uiModel.addAttribute("devices", Device.findAllDevices(sortFieldName, sortOrder));
		    }
    	}
    	
    	uiModel.addAttribute("searchString", searchString);
        uiModel.addAttribute("searchBySelected", searchBySelected);
        uiModel.addAttribute("listSearchBy", listSearchBy);
        uiModel.addAttribute("applications", Application.findAllApplications());
        return "manager/devices/list";
    }
    
}
