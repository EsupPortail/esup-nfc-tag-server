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

import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RequestMapping("/nfc")
@Controller
@Transactional
public class NfcRegisterController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
    @Resource
    private DeviceService deviceService;
	
	@Autowired
	private ApplicationsService applicationsService;

	@Resource
	private ApplicationDao applicationDao;

	@Resource
	private DeviceDao deviceDao;

	@RequestMapping("/locations")
	public String selectedLocationForm(
			@RequestParam(required = true) String imei, 
			@RequestParam(required = true) String macAddress,
			Model uiModel) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		log.info(eppn + "access to /nfc/locations");

		try {
			List<Application> applications = applicationsService.getApplications4Eppn(eppn, true);
			if (applications.isEmpty()) {
				log.info(eppn + " don't have location to manage");
				throw new AccessDeniedException(eppn + " don't have location to manage");
			}
			
			String numeroId = "?";
			
			List<Device> devices = deviceDao.findDevicesByEppnInitAndImeiEquals(eppn, imei).getResultList();
			if(!devices.isEmpty()) {
				Device device = devices.get(0);
				numeroId = device.getNumeroId();
			} 
			
			uiModel.addAttribute("numeroId", numeroId);
			uiModel.addAttribute("macAddress", macAddress);
			uiModel.addAttribute("imei", imei);
			uiModel.addAttribute("applications", applications);
		} catch (EmptyResultDataAccessException ex) {
			log.info(eppn + " is not manager");
			throw new AccessDeniedException(eppn + " is not manager");
		} catch (EsupNfcTagException e) {
			log.error("can't get locations", e);
		}
		return "templates/nfc/index";
	}

	@RequestMapping(value = "/register")
	public String nfcRegister(
			@RequestHeader(value="User-Agent") String userAgent, 
			@RequestParam(required = true) String location, 
			@RequestParam(required = true) Long applicationId,
			@RequestParam(required = true) String imei,
			@RequestParam(required = false) String macAddress, Model uiModel) throws IOException, EsupNfcTagException {
		
		Application application = applicationDao.findApplication(applicationId);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppnInit = auth.getName();
		
		// check right access ...
		if(!applicationsService.hasApplicationLocationRightAcces(eppnInit,  applicationId, location)) {
			log.warn(eppnInit + " can not register in this location " + location);
			throw new AccessDeniedException(eppnInit + " can not register in this location " + location);
		}
		
		String numeroId;
		if (deviceDao.countFindDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress)==0) {
			numeroId = deviceService.generateNumeroId();
			Device device = new Device();
			device.setNumeroId(numeroId);
			device.setEppnInit(eppnInit);
			device.setLocation(location);
			device.setApplication(application);
			device.setImei(imei);
			if(macAddress != null) device.setMacAddress(macAddress);
			device.setUserAgent(userAgent);
			if(application.getValidateAuthWoConfirmationDefault() != null){
				device.setValidateAuthWoConfirmation(application.getValidateAuthWoConfirmationDefault());
			} else{
				device.setValidateAuthWoConfirmation(false);
			}
			device.setCreateDate(new Date());
			deviceDao.persist(device);
		} else {
			Device tel = deviceDao.findDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress)
					.getSingleResult();
			numeroId = tel.getNumeroId();
		}
		uiModel.addAttribute("imei", imei);
		uiModel.addAttribute("macAddress", macAddress);
		uiModel.addAttribute("numeroId", numeroId);
		return "templates/nfc/register";
	}
	
	@RequestMapping(value = "/register-sgc")
	public String nfcRegisterSgc(
			@RequestHeader(value="User-Agent") String userAgent, 
			@RequestParam(required = true) String imei,
			@RequestParam(required = false) String macAddress, Model uiModel) throws IOException, EsupNfcTagException {
		log.info("start register sgc");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppnInit = auth.getName();
		Application writeSgcApplication = applicationsService.getSgcClientApplication4Eppn(eppnInit);
		String location = writeSgcApplication.getLocations().get(0);
		String numeroId;		
		
		// check right access ...
		if(!applicationsService.hasApplicationLocationRightAcces(eppnInit,  writeSgcApplication.getId(), location)) {
			log.warn(eppnInit + " can not register in write sgc");
			throw new AccessDeniedException(eppnInit + " can not register in write sgc");
		}

		long nbDevices = deviceDao.countFindDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress);
		if (nbDevices==0) {
			numeroId = deviceService.generateNumeroId();
			Device device = new Device();
			device.setNumeroId(numeroId);
			device.setEppnInit(eppnInit);
			device.setLocation(location);
			device.setApplication(writeSgcApplication);
			device.setImei(imei);
			if(macAddress != null) device.setMacAddress(macAddress);
			device.setUserAgent(userAgent);
			if(writeSgcApplication.getValidateAuthWoConfirmationDefault() != null){
				device.setValidateAuthWoConfirmation(writeSgcApplication.getValidateAuthWoConfirmationDefault());
			} else{
				device.setValidateAuthWoConfirmation(false);
			}
			device.setCreateDate(new Date());
			deviceDao.persist(device);
		} else if (nbDevices==1) {
			Device tel = deviceDao.findDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress)
					.getSingleResult();
			numeroId = tel.getNumeroId();
		} else {
			log.warn("more than one device for " + eppnInit + " in " + location + " with macAddress " + macAddress +
					" we should have only one device, so we remove the oldest");
			List<Device> devices = deviceDao.findDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress).getResultList();
			Collections.sort(devices, (d1, d2) -> d1.getLastUseDate()==null ? -1 : d2.getLastUseDate()==null ? 1 : d1.getLastUseDate().compareTo(d2.getLastUseDate()));
			for(int i = 0; i < devices.size()-1; i++) {
				deviceDao.remove(devices.get(i).getId());
			}
			numeroId = devices.get(devices.size()-1).getNumeroId();
		}

		String redir = "redirect:/nfc-index?numeroId=" + numeroId + "&imei=" + imei + "&macAddress=" + macAddress;
 		log.info("register done : " + redir);
		return redir;
	}
}
