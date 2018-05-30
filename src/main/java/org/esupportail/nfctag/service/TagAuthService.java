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
	
	public TagLog auth(TagType tagType, String tagId, String numeroId, String cardId, String appName, Boolean validate) throws EsupNfcTagException {
		Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
		device.setLastUseDate(new Date());
		device.merge();
		Application application = device.getApplication();
		TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(application.getTagIdCheck());
		if (tagIdCheckApi == null) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_tagidchecknotdefine);
		}
		TagLog tagLog = new TagLog();
		
		if(tagType != null){
			if(tagType.equals(TagType.DESFIRE)) {
				tagLog = tagIdCheckApi.getTagLogFromTagId(tagType, tagId, appName);
				tagLog.setDesfireId(tagId);
			}else if(tagType.equals(TagType.CSN)) {
				tagLog = tagIdCheckApi.getTagLogFromTagId(tagType, cardId, appName);
			}
		}
		tagLog.setCsn(cardId);
		tagLog.setNumeroId(device.getNumeroId());
		tagLog.setLocation(device.getLocation());
		tagLog.setApplicationName(device.getApplication().getName());
		tagLog.setAuthDate(new Date());
		tagLog.setNumeroId(device.getNumeroId());
		tagLog.setEppnInit(device.getEppnInit());
		tagLog.setStatus(TagLog.Status.none);
		tagLog.setLiveStatus(TagLog.Status.none);
		AppliExtApi extApi = applisExtService.get(application.getAppliExt());
		if(extApi!=null){
			extApi.isTagable(tagLog);
		}
		tagLog.persist();
		log.info("Enregitrement effectué : " + tagLog.getFirstname() + " " + tagLog.getLastname() + ", avec le terminal :" + numeroId);
		
		if(device.isValidateAuthWoConfirmation() && validate){
			Boolean validateTagOK =  this.validateTag(tagLog.getId(), numeroId);
			if(!validateTagOK){
				log.warn(EsupNfcTagErrorMessage.error_esupnfctagexception_tagvalidationerror.toString());
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_tagvalidationerror);
			}
		}
		
		if(device.isValidateAuthWoConfirmation() && !validate){
			Boolean cancelTagOK =  this.cancelTag(tagLog.getId(), numeroId);
			if(!cancelTagOK){
				log.warn(EsupNfcTagErrorMessage.error_esupnfctagexception_tagvalidationerror.toString());
				throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_tagvalidationerror);
			}
		}
		
		return tagLog;
	}
	
	public Boolean validateTag(Long tagId, String numeroId) {
		Boolean result = false;
		TagLog tagLog = TagLog.findTagLogsByIdAndNumeroIdEquals(tagId, numeroId).getSingleResult();
		Application app = Application.findApplicationsByNameEquals(tagLog.getApplicationName()).getSingleResult();
		AppliExtApi extApi = applisExtService.get(app.getAppliExt());
		if(extApi!=null && tagLog.getStatus().equals(TagLog.Status.none)) {
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

	public Boolean cancelTag(Long tagId, String numeroId) {
		Boolean cancelTagSuccess = false;
		TagLog tagLog = TagLog.findTagLogsByIdAndNumeroIdEquals(tagId, numeroId).getSingleResult();
		Application app = Application.findApplicationsByNameEquals(tagLog.getApplicationName()).getSingleResult();
		AppliExtApi extApi = applisExtService.get(app.getAppliExt());
		if(extApi!=null && tagLog.getStatus().equals(TagLog.Status.none)) {
			cancelTagSuccess = extApi.cancelTag(tagLog);
		}
		if(cancelTagSuccess){
			tagLog.setStatus(TagLog.Status.cancel);
			tagLog.merge();
			log.info("Status change to [" + tagLog.getStatus() + "] for " + tagLog.getEppn() );
		}else{
			log.info("Status don't change for " + tagLog.getEppn() );
		}
		return cancelTagSuccess;
	}
	
	public Boolean dismissTag(Long tagId, String numeroId) {
		TagLog tagLog = TagLog.findTagLogsByIdAndNumeroIdEquals(tagId, numeroId).getSingleResult();
		tagLog.setLiveStatus(TagLog.Status.valid);
		tagLog.merge();
		return true;
	}
	
	public String getEppnInit(TagType tagType, String numeroId) throws EsupNfcTagException {
		Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
		Application application = device.getApplication();
		TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(application.getTagIdCheck());
		if (tagIdCheckApi == null) {
			throw new EsupNfcTagException(EsupNfcTagErrorMessage.error_esupnfctagexception_tagidchecknotdefine);
		}
		return device.getEppnInit();
	}
	
	public String getDisplay(Long tagId, String numeroId) throws EsupNfcTagException {
		TagLog tagLog = TagLog.findTagLogsByIdAndNumeroIdEquals(tagId, numeroId).getSingleResult();
		Application app = Application.findApplicationsByNameEquals(tagLog.getApplicationName()).getSingleResult();
		AppliExtApi extApi = applisExtService.get(app.getAppliExt());
		return extApi.getDisplay(tagLog);
	}
	
}
