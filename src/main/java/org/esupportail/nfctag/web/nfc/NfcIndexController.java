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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.esupportail.nfctag.domain.AppLocation;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.VersionApkService;
import org.esupportail.nfctag.service.VersionJarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/nfc-index")
@Controller
@Transactional
public class NfcIndexController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	private VersionApkService versionApkService;

	@Resource
	private VersionJarService versionJarService;
	
	@Autowired
	private ApplicationsService applicationsService;
	
	@RequestMapping
	public String index(@RequestParam(required=false) String numeroId, 
			@RequestParam(required=false) String imei,
			@RequestParam(required=false) String macAddress,
			@RequestParam(required=false) String apkVersion,
			@RequestParam(required=false) String jarVersion) {
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
		
		if(numeroId==null || numeroId.isEmpty() || Device.findDevicesByNumeroIdEquals(numeroId).getResultList().isEmpty()) {
			return "redirect:/nfc/locations?imei=" + imei + "&macAddress=" + macAddress + "&apkVersion=" + versionApkService.getApkVersion() + "&jarVersion=" + versionJarService.getJarVersion();
		} else {
			return "redirect:/live?apkVersion=" + versionApkService.getApkVersion() + "&jarVersion=" + versionJarService.getJarVersion() + "&numeroId=" + numeroId;
		}
		
	}
	
	@RequestMapping(value = "/download")
	public String download(@RequestParam(required=false) String apkVersion,
						@RequestParam(required=false) String jarVersion, 
						Model uiModel, HttpServletRequest request) throws IOException {
		uiModel.addAttribute("apkVersion", apkVersion);
		uiModel.addAttribute("jarVersion", jarVersion);
		uiModel.addAttribute("requestUrl", request.getScheme() + "://" + request.getHeader("host"));
		return "nfc/download";
	}
	
	@RequestMapping(value = "/download-apk")
	public String downloadApk(Model uiModel, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String contentType = "application/vnd.android.package-archive";
	        response.setContentType(contentType);
	        ClassPathResource apkFile = new ClassPathResource("apk/esupnfctagdroid.apk");
	        response.setContentLength((int)apkFile.contentLength());
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + apkFile.getFilename() + "\"");
	        IOUtils.copy(apkFile.getInputStream(), response.getOutputStream());
	        return null;
		} catch(FileNotFoundException fnfe) {
			uiModel.addAttribute("notAvailableMessage", "L'application APK Android n'est actuellement pas disponible");
			return "apkJarNotAvailable";
		}
	}

	@RequestMapping(value = "/download-jar")
	public String downloadJar(Model uiModel, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String contentType = "application/vnd.android.package-archive";
	        response.setContentType(contentType);
	        ClassPathResource apkFile = new ClassPathResource("jar/esupnfctagdesktop.jar");
	        response.setContentLength((int)apkFile.contentLength());
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + apkFile.getFilename() + "\"");
	        IOUtils.copy(apkFile.getInputStream(), response.getOutputStream());
	        return null;
		} catch(FileNotFoundException fnfe) {
			uiModel.addAttribute("notAvailableMessage", "L'application Java n'est actuellement pas disponible");
			return "apkJarNotAvailable";
		}
	}
	
	/**
	 *  get Locations form without need to be authenticated ; but numeroId is needed
	 */
	@RequestMapping("/locations")
	public String selectedLocationForm(
			@RequestParam(required = true) String numeroId,
			Model uiModel) {
		
		log.info(numeroId + "access to /nfc-index/locations");
		
		List<Device> devices = Device.findDevicesByNumeroIdEquals(numeroId).getResultList();
		if(devices.isEmpty()) {
			return "redirect:/nfc-index";
		}

		Device device = devices.get(0);
		String eppn = device.getEppnInit();
		
		log.info("eppn init : " + eppn);
		
		try {
			List<AppLocation> appLocations = applicationsService.getAppsLocations4Eppn(eppn);
			if (appLocations.isEmpty()) {
				log.info(eppn + " don't have location to manage");
				throw new AccessDeniedException(eppn + " don't have location to manage");
			}
			uiModel.addAttribute("numeroId", numeroId);
			uiModel.addAttribute("macAddress", device.getMacAddress());
			uiModel.addAttribute("imei", device.getImei());
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
	
	
	/**
	 *  register without need to be authenticated ; but numeroId is needed
	 */
	@RequestMapping(value = "/register")
	public String nfcRegister(
			@RequestHeader(value="User-Agent") String userAgent, 
			@RequestParam(required = true) String numeroId,
			@RequestParam(required = true) String location, 
			@RequestParam(required = true) Long applicationId,
			@RequestParam(required = true) String imei,
			@RequestParam(required = true) String macAddress,
			Model uiModel, HttpServletRequest httpServletRequest) throws IOException, EsupNfcTagException {
		
		log.info(numeroId + "access to /nfc-index/locations");
		
		List<Device> devices = Device.findDevicesByNumeroIdEquals(numeroId).getResultList();
		if(devices.isEmpty()) {
			return "redirect:/nfc/register/?location=" + encodeUrlPathSegment(location, httpServletRequest) + "&applicationId=" + applicationId + "&imei=" + imei + "&macAddress=" + macAddress + "&apkVersion=" + versionApkService.getApkVersion() + "&jarVersion=" + versionJarService.getJarVersion();
		}

		Device device = devices.get(0);
		String eppnInit = device.getEppnInit();
		
		Application application = Application.findApplication(applicationId);
		
		// check right access ...
		List<AppLocation> appLocations = applicationsService.getAppsLocations4Eppn(eppnInit);
		Optional<AppLocation> appLocation = appLocations.stream().filter(appLoc -> appLoc.getApplication().getId().equals(applicationId)).findAny();
		if(!appLocation.isPresent() || !appLocation.get().getLocations().contains(location)) {
			log.warn(eppnInit + " can not register in this location " + location);
			throw new AccessDeniedException(eppnInit + " can not register in this location " + location);
		}

		device.setLocation(location);
		device.setApplication(application);
		device.setMacAddress(macAddress);
		if(application.getValidateAuthWoConfirmationDefault() != null){
			device.setValidateAuthWoConfirmation(application.getValidateAuthWoConfirmationDefault());
		} else{
			device.setValidateAuthWoConfirmation(false);
		}
		device.merge();
		
		uiModel.addAttribute("imei", imei);
		uiModel.addAttribute("macAddress", macAddress);
		uiModel.addAttribute("numeroId", numeroId);
		uiModel.addAttribute("apkVersion", versionApkService.getApkVersion());
		uiModel.addAttribute("jarVersion", versionJarService.getJarVersion());
		return "nfc/register";
	}

	@RequestMapping(value = "/unregister")
	public String nfcUnRegister(
			@RequestParam(required = true) String numeroId, 
			@RequestParam(required = true) String imei, 
			@RequestParam(required = true) String macAddress,
			@RequestParam(required = true) Boolean full,
			Model uiModel) throws IOException {
		uiModel.addAttribute("imei", imei);
		uiModel.addAttribute("macAddress", macAddress);
		uiModel.addAttribute("numeroId", numeroId);
		uiModel.addAttribute("apkVersion", versionApkService.getApkVersion());
		uiModel.addAttribute("jarVersion", versionJarService.getJarVersion());
		
		if(Device.countFindDevicesByNumeroIdEquals(numeroId)>0) {
			Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
			if(full) {
				device.remove();
			} else {
				device.setLocation(null);
				device.setApplication(null);
				device.setValidateAuthWoConfirmation(false);
				device.merge();
			}
		 }
		 
		return "nfc/unregister";
	}
	
    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
	} catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }

}


