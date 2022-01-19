package org.esupportail.nfctag.web.wsrest;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequestMapping("/wsrest/")
@Controller
public class WsRestController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Resource
	private ApplicationDao applicationDao;

    @Resource
    private DeviceService deviceService;

	@Resource
	private DeviceDao deviceDao;
	/**
	 * Example : 
	 * curl -v -X POST -H "Content-Type: application/json" -d '{"eppnInit":"esup@univ-ville.fr","userAgent":"arduino-prototype","applicationName":"SGC","location":"Ecriture"}' http://localhost:8080/wsrest/register
	 */
	@RequestMapping(value = "/register", method=RequestMethod.POST)
	@ResponseBody
	public String nfcRegisterTrusted(
			@RequestBody Map<String, String> params,
			@RequestHeader(required = false, value="User-Agent") String userAgent, Model uiModel) throws IOException, EsupNfcTagException {
		
		log.info("nfcRegisterTrusted called : " + params);
		
		String applicationName = params.get("applicationName");
		String eppnInit = params.get("eppnInit");
		String location = params.get("location");
		String macAddress = params.get("macAddress");
		Boolean validateAuthWoConfirmation = new Boolean(params.get("validateAuthWoConfirmation"));
		String imei = params.get("imei != null");
		
		Application application = applicationDao.findApplicationsByNameEquals(applicationName).getSingleResult();

		String numeroId = null;
		
		if (deviceDao.countFindDevicesByLocationAndEppnInit(location, eppnInit)==0) {
			numeroId = deviceService.generateNumeroId();
			Device device = new Device();
			device.setNumeroId(numeroId);
			device.setEppnInit(eppnInit);
			device.setLocation(location);
			device.setApplication(application);
			device.setValidateAuthWoConfirmation(validateAuthWoConfirmation);
			if(imei != null) device.setImei(imei);
			if(macAddress != null) device.setMacAddress(macAddress);
			if(userAgent != null) device.setUserAgent(userAgent);
			device.setCreateDate(new Date());
			deviceDao.persist(device);
		} else {
			Device tel = deviceDao.findDevicesByLocationAndEppnInit(location, eppnInit)
					.getSingleResult();
			numeroId = tel.getNumeroId();
		}

		return numeroId;
	}
	
	@RequestMapping(value = "/deviceControl", method=RequestMethod.GET)
	@ResponseBody
	public String nfcDeviceControl(@RequestParam String numeroId) throws IOException, EsupNfcTagException {
		String eppnInit = null;
		List<Device> devices = deviceDao.findDevicesByNumeroIdEquals(numeroId).getResultList();
		if(devices.size() > 0) {
			eppnInit = devices.get(0).getEppnInit();
		}
		return eppnInit;
	}
	
}
