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
		log.info("device identify :" + numeroId);
		Application application = devices.get(0).getApplication();
		if(application == null) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_locationnotfound);
		}
		return application;
	}

	public List<AppLocation> getAppsLocations4Eppn(String eppn) throws EsupNfcTagException {
		List<AppLocation> appLocations = new ArrayList<AppLocation>();
		for(Application appli: Application.findAllApplications()) {
			log.info("Get apps locations of " + appli.getName() + " for " + eppn);
			AppliExtApi appliExtApi = applisExtService.get(appli.getAppliExt());
			AppLocation appLocation = new AppLocation(appli, true);
			try {
				List<String> locations = appliExtApi.getLocations4Eppn(eppn);
				if(locations!=null && appli.isActive()) { 
					appLocation.setLocations(locations);
				} else {
					appLocation.setAvailable(false);
				}
			} catch(Exception e) {
				log.error("Exception on" + appli.getName() + "!", e);
				appLocation.setAvailable(false);
			}
			appLocations.add(appLocation);
		}
		return appLocations;
	}
	
}
