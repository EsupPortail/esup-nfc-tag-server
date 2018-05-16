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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.nfctag.domain.AppLocation;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationsService {
	
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Resource
	ApplisExtService applisExtService;
    

	public Application getApplicationFromNumeroId(String numeroId) throws EsupNfcTagException {

		List<Device> devices = Device.findDevicesByNumeroIdEquals(numeroId).getResultList();
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

	public List<AppLocation> getAppsLocations4Eppn(String eppn, boolean checkActive) throws EsupNfcTagException {
		List<AppLocation> appLocations = new ArrayList<AppLocation>();
		for(Application appli: Application.findAllApplications()) {
			AppliExtApi appliExtApi = applisExtService.get(appli.getAppliExt());
			AppLocation appLocation = new AppLocation(appli, true);
			try {
				List<String> locations = appliExtApi.getLocations4Eppn(eppn);
				if(locations.size()>0 && (appli.isActive() || !checkActive)) { 
					appLocation.setLocations(locations);
				}
			} catch(Exception e) {
				log.error("Exception on" + appli.getName() + "!");
				appLocation.setAvailable(false);
			}
			appLocations.add(appLocation);
		}
		return appLocations;
	}
	
	public List<AppLocation> getAppLocations4Eppn(String eppn, long applicationId, boolean checkActive) throws EsupNfcTagException {
		List<AppLocation> appLocations = new ArrayList<AppLocation>();
		Application appli = Application.findApplication(applicationId);
		AppliExtApi appliExtApi = applisExtService.get(appli.getAppliExt());
		AppLocation appLocation = new AppLocation(appli, true);
		try {
			List<String> locations = appliExtApi.getLocations4Eppn(eppn);
			if(locations.size()>0 && (appli.isActive() || !checkActive)) { 
				appLocation.setLocations(locations);
			}
		} catch(Exception e) {
			log.error("Exception on" + appli.getName() + "!");
			appLocation.setAvailable(false);
		}
		appLocations.add(appLocation);
		return appLocations;
	}
	
}
