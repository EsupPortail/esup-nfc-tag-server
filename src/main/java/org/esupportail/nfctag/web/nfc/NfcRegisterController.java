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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.AppLocation;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.DeviceService;
import org.esupportail.nfctag.service.VersionApkService;
import org.esupportail.nfctag.service.VersionJarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/nfc")
@Controller
@Transactional
public class NfcRegisterController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	private VersionApkService versionApkService;

	@Resource
	private VersionJarService versionJarService;
	
    @Resource
    private DeviceService deviceService;
	
	@Autowired
	private ApplicationsService applicationsService;

	@RequestMapping("/locations")
	public String selectedLocationForm(
			@RequestParam(required = true) String imei, 
			@RequestParam(required = true) String macAddress,
			@RequestParam(required = false) String apkVersion,
			@RequestParam(required = false) String jarVersion,
			Model uiModel) {
		if(imei.equals("appliJava")){
			if(jarVersion != null){
				if(!versionJarService.isUserJarVersionUp2Date(jarVersion)){
					return "redirect:/nfc-index/download?jarVersion=" + versionJarService.getJarVersion();
				}
			}else{
				return "redirect:/nfc-index/download?jarVersion=" + versionJarService.getJarVersion();
			}
		}else{
			if(apkVersion != null){
				if(!versionApkService.isUserApkVersionUp2Date(apkVersion)){
					return "redirect:/nfc-index/download?apkVersion=" + versionApkService.getApkVersion();
				}
			}else{
				return "redirect:/nfc-index/download?apkVersion=" + versionApkService.getApkVersion();
			}
		}
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		log.info(eppn + "access to /nfc/locations");

		try {
			List<AppLocation> appLocations = applicationsService.getAppsLocations4Eppn(eppn, true);
			if (appLocations.isEmpty()) {
				log.info(eppn + " don't have location to manage");
				throw new AccessDeniedException(eppn + " don't have location to manage");
			}
			
			String numeroId = "?";
			
			List<Device> devices = Device.findDevicesByEppnInitAndImeiEquals(eppn, imei).getResultList();
			if(!devices.isEmpty()) {
				Device device = devices.get(0);
				numeroId = device.getNumeroId();
			} 
			
			uiModel.addAttribute("numeroId", numeroId);
			uiModel.addAttribute("macAddress", macAddress);
			uiModel.addAttribute("imei", imei);
			uiModel.addAttribute("appLocations", appLocations);
			uiModel.addAttribute("apkVersion", versionApkService.getApkVersion());
			uiModel.addAttribute("jarVersion", versionJarService.getJarVersion());
		} catch (EmptyResultDataAccessException ex) {
			log.info(eppn + " is not manager");
			throw new AccessDeniedException(eppn + " is not manager");
		} catch (EsupNfcTagException e) {
			log.error("can't get locations", e);
		}
		return "nfc";
	}

	@RequestMapping(value = "/register")
	public String nfcRegister(
			@RequestHeader(value="User-Agent") String userAgent, 
			@RequestParam(required = true) String location, 
			@RequestParam(required = true) Long applicationId,
			@RequestParam(required = true) String imei,
			@RequestParam(required = true) String macAddress, Model uiModel) throws IOException, EsupNfcTagException {
		
		Application application = Application.findApplication(applicationId);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppnInit = auth.getName();
		
		// check right access ...
		List<AppLocation> appLocations = applicationsService.getAppsLocations4Eppn(eppnInit, true);
		Optional<AppLocation> appLocation = appLocations.stream().filter(appLoc -> appLoc.getApplication().getId().equals(applicationId)).findAny();
		if(!appLocation.isPresent() || !appLocation.get().getLocations().contains(location)) {
			log.warn(eppnInit + " can not register in this location " + location);
			throw new AccessDeniedException(eppnInit + " can not register in this location " + location);
		}
		
		String numeroId;
		
		if (Device.countFindDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress)==0) {
			numeroId = deviceService.generateNumeroId();
			Device device = new Device();
			device.setNumeroId(numeroId);
			device.setEppnInit(eppnInit);
			device.setLocation(location);
			device.setApplication(application);
			device.setImei(imei);
			device.setMacAddress(macAddress);
			device.setUserAgent(userAgent);
			if(application.getValidateAuthWoConfirmationDefault() != null){
				device.setValidateAuthWoConfirmation(application.getValidateAuthWoConfirmationDefault());
			} else{
				device.setValidateAuthWoConfirmation(false);
			}
			device.setCreateDate(new Date());
			device.persist();
		} else {
			Device tel = Device.findDevicesByLocationAndEppnInitAndMacAddressEquals(location, eppnInit, macAddress)
					.getSingleResult();
			numeroId = tel.getNumeroId();
		}
		uiModel.addAttribute("imei", imei);
		uiModel.addAttribute("macAddress", macAddress);
		uiModel.addAttribute("numeroId", numeroId);
		uiModel.addAttribute("apkVersion", versionApkService.getApkVersion());
		uiModel.addAttribute("jarVersion", versionJarService.getJarVersion());
		return "nfc/register";
	}
	
}
