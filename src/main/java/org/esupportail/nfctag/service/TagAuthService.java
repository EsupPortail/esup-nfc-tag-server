package org.esupportail.nfctag.service;

import java.util.Date;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.esupportail.nfctag.service.api.TagIdCheckApi;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class TagAuthService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	ApplisExtService applisExtService;

	@Resource
	TagIdCheckService tagIdCheckService;
	
	@Resource
	ApplicationsService applicationsService;
	
	public TagLog auth(TagType tagType, String tagId, String numeroId) throws EsupNfcTagException {
		Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
		Application application = device.getApplication();
		TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(application.getTagIdCheck());
		if (tagIdCheckApi == null) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_tagidchecknotdefine);
		}
		TagLog tagLog = tagIdCheckApi.getTagLogFromTagId(tagType, tagId);
		tagLog.setNumeroId(device.getNumeroId());
		tagLog.setLocation(device.getLocation());
		tagLog.setApplicationName(device.getApplication().getName());
		tagLog.setAuthDate(new Date());
		tagLog.setNumeroId(device.getNumeroId());
		tagLog.setEppnInit(device.getEppnInit());
		tagLog.setStatus(TagLog.Status.none);
		AppliExtApi extApi = applisExtService.get(application.getAppliExt());
		if(extApi!=null){
			extApi.isTagable(tagLog);
		}
		tagLog.persist();
		log.info("Enregitrement effectué : " + tagLog.getFirstname() + " " + tagLog.getLastname() + ", avec le terminal :" + numeroId);
		
		if(device.isValidateAuthWoConfirmation() && !this.validateTag(tagLog.getId())) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_tagvalidationerror);
		}
		
		return tagLog;
	}

	public Boolean validateTag(Long tagId) {
		Boolean result = false;
		TagLog tagLog = TagLog.findTagLog(tagId);
		Application app = Application.findApplicationsByNameEquals(tagLog.getApplicationName()).getSingleResult();
		AppliExtApi extApi = applisExtService.get(app.getAppliExt());
		if(extApi!=null) {
			result = extApi.validateTag(tagLog);
		}
		if(result){
			tagLog.setStatus(TagLog.Status.valid);
			tagLog.merge();
			log.info("Status change to [" + tagLog.getStatus() + "] for " + tagLog.getEppn() );
		}else{
			log.info("Status don't change for " + tagLog.getEppn() );
		}
		return result;
	}

	public Boolean cancelTag(Long tagId) {
		Boolean cancelTagSuccess = false;
		TagLog tagLog = TagLog.findTagLog(tagId);
		Application app = Application.findApplicationsByNameEquals(tagLog.getApplicationName()).getSingleResult();
		AppliExtApi extApi = applisExtService.get(app.getAppliExt());
		if(extApi!=null) {
			cancelTagSuccess = extApi.cancelTag(tagLog);
		}
		if(cancelTagSuccess){
			tagLog.setStatus(TagLog.Status.cancel);
			tagLog.merge();
			log.info("Status change to [" + tagLog.getStatus() + "] for " + tagLog.getEppn() );
		}else{
			log.info("Status don't change for " + tagLog.getEppn() );
		}
		log.info("Status change to [" + tagLog.getStatus() + "] for " + tagLog.getEppn() );
		return cancelTagSuccess;
	}


}
