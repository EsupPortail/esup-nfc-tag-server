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
package org.esupportail.nfctag.service;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationsService {
	
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Resource
	ApplisExtService applisExtService;
    
	@Resource
	NfcAuthConfigService nfcAuthConfigService;
	
	@Resource
	TagIdCheckService tagIdCheckService;

	@Resource
	private ApplicationDao applicationDao;

	@Resource
	private DeviceDao deviceDao;

	public Application getApplicationFromNumeroId(String numeroId) throws EsupNfcTagException {

		List<Device> devices = deviceDao.findDevicesByNumeroIdEquals(numeroId).getResultList();
		if(devices.isEmpty()) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_unknowdevice);
		}
		log.trace("device identify :" + numeroId);
		Application application = devices.get(0).getApplication();
		if(application == null) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_locationnotfound);
		}
		return application;
	}
	
	public List<Application> getApplications4Eppn(String eppn, boolean checkActive) throws EsupNfcTagException {
		List<Application> applications = new ArrayList<Application>();
		for(Application appli: applicationDao.findAllApplications()) {
			AppliExtApi appliExtApi = applisExtService.get(appli.getAppliExt());
			try {
				List<String> locations = appliExtApi.getLocations4Eppn(eppn);
				if(locations.size()>0 && (appli.isActive() || !checkActive)) { 
					appli.setLocations(locations);
				}
			} catch(Exception e) {
				log.error("Exception on" + appli.getName() + "!");
				appli.setAvailable(false);
			}
			if(!appli.getLocations().isEmpty() || !appli.getAvailable()) {
				applications.add(appli);
			}
		}
		applications.sort((a1, a2) -> { 
			int cmp = a2.getAvailable().compareTo(a1.getAvailable());
			if(cmp==0) {
				cmp =  Integer.compare(a2.getLocations().size(), a1.getLocations().size());
			}
			if(cmp==0) {
				if(a1.getLocations().size()==1 && a2.getLocations().size()==1) {
					cmp =  a1.getLocations().get(0).compareTo( a2.getLocations().get(0));
				} else {
					cmp =  a1.getName().compareTo(a2.getName());
				}
			}
			return cmp;
		});
		return applications;
	}
	
	
	public Application getSgcClientApplication4Eppn(String eppn) throws EsupNfcTagException {
		List<Application> applications = applicationDao.findApplicationsBySgcClientApp(true).getResultList();
		if(applications.isEmpty()) {
			throw new AccessDeniedException("Pas d'application d'écriture pour esup-sgc-client de trouvé");
		}
		if(applications.size()>1) {
			log.warn("Plus d'1 application d'écriture pour esup-sgc-client de trouvées !?");
		}
		Application appli = applications.get(0);
		AppliExtApi appliExtApi = applisExtService.get(appli.getAppliExt());
		appli.setLocations(appliExtApi.getLocations4Eppn(eppn));
		return appli;
	}
	

	public boolean hasApplicationLocationRightAcces(String eppn, Long applicationId, String location) {
		Application appli = applicationDao.findApplication(applicationId);
		AppliExtApi appliExtApi = applisExtService.get(appli.getAppliExt());
		try {
			List<String> locations = appliExtApi.getLocations4Eppn(eppn);
			return locations.contains(location);
		} catch(Exception e) {
			log.error("Exception on" + appli.getName() + "!");
		}
		return false;
	}


	public boolean checkApplicationFromNumeroId(String numeroId) throws EsupNfcTagException {
		boolean isDeviceValid = false;
		List<Device> devices = deviceDao.findDevicesByNumeroIdEquals(numeroId).getResultList();
		if(!devices.isEmpty()) {
			log.trace("device identify :" + numeroId);
			Application application = devices.get(0).getApplication();
			if(application != null) {
				if(checkApplication(application.getId())){
					isDeviceValid = true;
				}
			}
		}
		return isDeviceValid;
	}
	
	public boolean checkApplication(long id) throws EsupNfcTagException {
		boolean isApplicationValid = false;
		Application application = applicationDao.findApplication(id);
		if(!application.getAppliExt().isEmpty() && !application.getTagIdCheck().isEmpty() && !application.getNfcConfig().isEmpty()){
			AppliExtApi extApi = applisExtService.get(application.getAppliExt());
			NfcAuthConfig nfcAuthConfig = nfcAuthConfigService.get(application.getNfcConfig());
			TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(application.getTagIdCheck());
			if(extApi.getDescription() != null && nfcAuthConfig.getAuthType() != null && tagIdCheckApi.getDescription() != null) {
				isApplicationValid = true;
			}
		}
		return isApplicationValid;
	}
}

