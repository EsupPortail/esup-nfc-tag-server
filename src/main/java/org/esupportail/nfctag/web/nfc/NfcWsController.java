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
package org.esupportail.nfctag.web.nfc;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.service.TagAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/nfc-ws")
@Controller
@Transactional
public class NfcWsController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	TagAuthService tagAuthService;
	
	@RequestMapping(value="/validate",params = {"id"})
	@ResponseBody
	public Boolean validateTag(long id, Model uiModel) throws IOException {
		return tagAuthService.validateTag(id);
	}

	@RequestMapping(value="/cancel",params = {"id"})
	@ResponseBody
	public Boolean cancelTag(long id, Model uiModel) throws IOException {
		return tagAuthService.cancelTag(id);
	}

	@RequestMapping(value="/display",params = {"id"})
	@ResponseBody
	public String getDisplay(long id, Model uiModel) throws IOException {
		log.info("get display");
		return tagAuthService.getDisplay(id);
	}
	
	@RequestMapping(value="/location", produces = "application/json")
	@ResponseBody
	public String getLocation(
			@RequestParam(required = true) String numeroId, 
			Model uiModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		log.info(eppn + "access to /nfc/locations");
		String locationName = ""; 
		try {
			
			List<Device> devices = Device.findDevicesByNumeroIdEquals(numeroId).getResultList();
			if(!devices.isEmpty()) {
				Device device = devices.get(0);
				locationName = device.getLocation();
				
			} 
		} catch (EmptyResultDataAccessException ex) {
			log.info(eppn + " is not manager");
			throw new AccessDeniedException(eppn + " is not manager");
		}
		return "{"+locationName+"}";
	}
}


