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

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.CODE;
import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service;
import org.esupportail.nfctag.service.desfire.DesfireAuthSession;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.actions.DesfireActionService;
//import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.web.live.ErrorLongPoolController;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/desfire-ws")
@Scope(value="session")
public class DesfireWsController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	DesfireService desfireService;

	@Resource
	TagAuthService tagAuthService;

	@Resource 
	LiveLongPoolController liveController;

	@Resource 
	ErrorLongPoolController errorLongPoolController;

	@Resource
	DesfireAuthSession desfireAuthSession;

	@Resource
	ApplicationsService applicationsService;

	// session scope
	String eppnInit;
	DesfireActionService desfireActionService;

	@RequestMapping(produces = "application/json")
	@ResponseBody
	public NfcResultBean process(@RequestParam String numeroId, @RequestParam String cardId, @RequestParam String result, HttpSession session) {

		if(!applicationsService.checkApplicationFromNumeroId(numeroId)){
			throw new EsupNfcTagException("device error for " + numeroId + " please check configuration", numeroId);
		}

		if(desfireService.getStep()==null) {
			eppnInit = tagAuthService.getEppnInit(TagType.DESFIRE, numeroId);
			desfireService.setNumeroId(numeroId);
			desfireActionService = desfireAuthSession.getDesfireAuthConfig().getDesfireActionService(desfireService, tagAuthService, liveController);
			log.debug("INIT " + desfireActionService.getFunction() + " on cardId : " + cardId + " by " + eppnInit);
		}

		if("ERROR".equals(result)) {
			throw new EsupNfcTagException(result, numeroId);			
		}
		DESFireEV1Service.Response response = DESFireEV1Service.Response.UNKNOWN_CODE;

		if(result.length() > 2 ){
			String msg = result.substring(result.length() - 2);
			response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			log.debug("return is : " + result + " with Desfire Response : " + response.toString());
			if(!response.equals(DESFireEV1Service.Response.OPERATION_OK) && !response.equals(DESFireEV1Service.Response.ADDITIONAL_FRAME)) {
				throw new EsupNfcTagException("Error desfire response : " + response.toString(), numeroId);
			}
		}

		NfcResultBean nfcResultBean = desfireActionService.process(eppnInit, response, numeroId, cardId, result);

		nfcResultBean.setjSessionId(session.getId());
		log.trace(nfcResultBean.getMsg());
		log.debug("command for card must be : " + nfcResultBean.getFullApdu());
		log.debug("next step will be : " + desfireService.getStep() + " with auth step : " + desfireService.getAuthStep());
		return nfcResultBean;
	}

	@ExceptionHandler(EsupNfcTagException.class)
	@ResponseBody
	public NfcResultBean handleException(EsupNfcTagException e) {
		NfcResultBean nfcResultBean = new NfcResultBean();
		desfireService.reset();
		TagError tagError = new TagError();
		tagError.setNumeroId(e.getNumeroId());
		tagError = new TagError(e);
		log.error("desfire error : " + e.getMessage());
		nfcResultBean.setMsg(e.getMessage());		
		errorLongPoolController.handleError(tagError);
		nfcResultBean.setCode(CODE.ERROR);
		return nfcResultBean;
	}

}
