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
package org.esupportail.nfctag.web.live;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.ApplisExtService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.VersionApkService;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

@RequestMapping("/live")
@Controller
@Transactional
public class LiveLongPoolController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	NfcAuthConfigService nfcAuthConfigService;
	
	@Resource
	VersionApkService versionApkService;

	@Resource
	ApplisExtService applisExtService;
	
	private Map<DeferredResult<List<TagLog>>, LiveQuery> suspendedLeoAuthsRequests = new ConcurrentHashMap<DeferredResult<List<TagLog>>, LiveQuery>();

	private Queue<TagLog> tagLogs = new PriorityBlockingQueue<TagLog>(100, new TagLogComparator());
	

    private List<String> ipsStart4LiveFullAnonymousList;

    @Value("${ipsStart4LiveFullAnonymous}")
    public void setipsStart4LiveFullAnonymous(String ipsStart4LiveFullAnonymous) {
    	ipsStart4LiveFullAnonymousList = Arrays.asList(ipsStart4LiveFullAnonymous.split(" "));
        log.info("Restricted access from this (started) ip for full live without authentication : " + ipsStart4LiveFullAnonymousList);
    }
	
	@RequestMapping
	public String index(@RequestParam(required=false) String numeroId, @RequestParam(required=false) String apkVersion, Model uiModel){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(!auth.isAuthenticated()) {
			return "redirect:/manager";
		}

		if(numeroId!=null) {
			if(Device.countFindDevicesByNumeroIdEquals(numeroId)<1 || Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult().getApplication() == null) {
				return "redirect:/nfc-index/locations?apkVersion=" + apkVersion + "&numeroId=" + numeroId;		
			}else{
				Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
				Application app = device.getApplication();
				String nfcAuthConfigKey = app.getNfcConfig(); 
				NfcAuthConfig nfcAuthConfig = nfcAuthConfigService.get(nfcAuthConfigKey);
				AppliExtApi appliExtApi = applisExtService.get(app.getAppliExt());
				
				uiModel.addAttribute("pageHeader", appliExtApi.getHeader());
				uiModel.addAttribute("backgroundColor", appliExtApi.getBackgroundColor());
				uiModel.addAttribute("isActiv", app.isActive());
				uiModel.addAttribute("imei", device.getImei());
				uiModel.addAttribute("macAddress", device.getMacAddress());
				uiModel.addAttribute("location", device.getLocation());
				uiModel.addAttribute("authType", nfcAuthConfig.getAuthType());
				uiModel.addAttribute("validateAuthWoConfirmation", device.isValidateAuthWoConfirmation());
				uiModel.addAttribute("numeroId", numeroId);
				return "live/mobil";
			}
		} else {		
			if(!isLiveLongPoolAuthorized(auth)) {
				String msg = " not authorized to see the full live ";
				log.warn(auth.getName() + msg);
				throw new AccessDeniedException(auth.getName() + msg);
			}
		}
		
		return "live/index";		
	}

	@RequestMapping(value = "/taglogs")
	@ResponseBody
	public DeferredResult<List<TagLog>> listLeoAuth(@RequestParam Long authDateTimestamp, @RequestParam(required=false) String numeroId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// admin ou tel
		if(numeroId==null && !isLiveLongPoolAuthorized(auth)) {
			return null;
		}

		LiveQuery liveQuery = new LiveQuery(authDateTimestamp, numeroId);
		
		final DeferredResult<List<TagLog>> tagLogs = new DeferredResult<List<TagLog>>(null, Collections.emptyList());
		this.suspendedLeoAuthsRequests.put(tagLogs, liveQuery);

		tagLogs.onCompletion(new Runnable() {
			public void run() {
				suspendedLeoAuthsRequests.remove(tagLogs);
			}
		});
		
		List<TagLog> list = getLatestLeoAuths(liveQuery);
		if (!list.isEmpty()) {
			tagLogs.setResult(list);
		}

		return tagLogs;
	}

	private List<TagLog> getLatestLeoAuths(LiveQuery liveQuery) {

		List<TagLog> tagLogs = new ArrayList<TagLog>(); 

		String sortFieldName = "authDate";
		String sortOrder = "desc";
		
		Long authDateTimestamp = liveQuery.getAuthDateTimestamp();
		String numeroId = liveQuery.getNumeroId();
		
		if(authDateTimestamp != 0) {
			Date authDate = new Date(authDateTimestamp);
			if(numeroId != null && !numeroId.isEmpty()) {
				List<Device> devices = Device.findDevicesByNumeroIdEquals(numeroId).getResultList();
				if(devices.isEmpty() || devices.get(0).getApplicationName().isEmpty() || devices.get(0).getLocation().isEmpty()) {
					tagLogs = TagLog.findTagLogsByAuthDateGreaterThanAndNumeroIdEquals(authDate, numeroId, sortFieldName, sortOrder).getResultList();
				} else {
					tagLogs = TagLog.findTagLogsByAuthDateGreaterThanAndNumeroIdEqualsAndApplicationNameEqualsAndLocationEquals(authDate, numeroId, devices.get(0).getApplicationName(), devices.get(0).getLocation(), sortFieldName, sortOrder).getResultList();
				}
			} else {
				tagLogs = TagLog.findTagLogsByAuthDateGreaterThan(authDate, sortFieldName, sortOrder).getResultList();
			}
		} else {
			int firstResult = 0;
			int size = 10;
			if(numeroId != null && !numeroId.isEmpty()) {
				List<Device> devices = Device.findDevicesByNumeroIdEquals(numeroId).getResultList();
				if(devices.isEmpty() || devices.get(0).getApplicationName().isEmpty() || devices.get(0).getLocation().isEmpty()) {
					tagLogs = TagLog.findTagLogsByNumeroIdEquals(numeroId, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(size).getResultList();
				} else {
					tagLogs = TagLog.findTagLogsByNumeroIdEqualsAndApplicationNameEqualsAndLocationEquals(numeroId, devices.get(0).getApplicationName(), devices.get(0).getLocation(), sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(size).getResultList();
				}
			} else {
				tagLogs = TagLog.findTagLogEntries(firstResult, size, sortFieldName, sortOrder);
			}
		}
		
		return tagLogs;
	}

	public void handleTagLog(TagLog tagLog) {
		log.debug("Client received: "+tagLog.getId() +" " + tagLog.getEppn() + " with status = " + tagLog.getStatus());
		this.tagLogs.add(tagLog);
		for (Entry<DeferredResult<List<TagLog>>, LiveQuery> entry : this.suspendedLeoAuthsRequests.entrySet()) {
			List<TagLog> newLeoAuths = getLatestLeoAuths(entry.getValue());
			entry.getKey().setResult(newLeoAuths);
		}
	}

	private static class TagLogComparator implements Comparator<TagLog> {
		public int compare(TagLog o1, TagLog o2) {
			return o1.getAuthDate().compareTo(o2.getAuthDate());
		}
	}
	
	private boolean isLiveLongPoolAuthorized(Authentication auth) {
		return auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_ADMIN")) || auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_SUPERVISOR")) || isIpCanBeUsed4LiveFullAnonymous(auth);
	}
	
	private Boolean isIpCanBeUsed4LiveFullAnonymous(Authentication auth) {
		WebAuthenticationDetails wad = (WebAuthenticationDetails) auth.getDetails();
        String userIPAddress = wad.getRemoteAddress();
		if (ipsStart4LiveFullAnonymousList != null && !ipsStart4LiveFullAnonymousList.isEmpty()) {
			for (String ipStart : ipsStart4LiveFullAnonymousList) {
				if (userIPAddress.startsWith(ipStart)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Device> getDevices() {
		List<Device> devices = new ArrayList<Device>();	
		for (Entry<DeferredResult<List<TagLog>>, LiveQuery> entry : this.suspendedLeoAuthsRequests.entrySet()) {
			String numeroId = entry.getValue().getNumeroId();
			if(numeroId != null && !numeroId.isEmpty()) {
				Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
				Date lastPollDate = entry.getValue().getLastPollDate();
				device.setLastPollDate(lastPollDate);
				devices.add(device);
			}
		}
		Collections.sort(devices, (Device c1, Device c2) -> c2.getLastPollDate().compareTo(c1.getLastPollDate()));
		return devices;
	}


}
