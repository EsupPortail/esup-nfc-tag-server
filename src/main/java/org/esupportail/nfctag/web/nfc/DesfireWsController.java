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

import org.esupportail.nfctag.beans.DesfireTag;
import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.Action;
import org.esupportail.nfctag.beans.NfcResultBean.CODE;
import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.api.impl.DesfireUpdateConfig;
import org.esupportail.nfctag.service.api.impl.DesfireWriteConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service;
import org.esupportail.nfctag.service.desfire.DesfireAuthSession;
import org.esupportail.nfctag.service.desfire.DesfireService;
//import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.web.live.ErrorLongPoolController;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

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
	
    // session scope
    String eppnInit;
    Boolean update = false;
    Boolean write = false;

	@RequestMapping(produces = "application/json")
	@ResponseBody
	public NfcResultBean process(@RequestParam String numeroId, @RequestParam String cardId, @RequestParam String result, HttpSession session) throws EsupNfcTagException, JsonProcessingException {

		if(desfireService.getStep()==null) {
			eppnInit = tagAuthService.getEppnInit(TagType.DESFIRE, numeroId);
		}

		
		log.trace("result for step " + desfireService.getStep() + " was : " + result );
		
		
		NfcResultBean nfcResultBean = new NfcResultBean();
				
		desfireService.setNumeroId(numeroId);
		//String command = "";
		
		String function = "READ";
		if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireReadConfig){
			function = "READ";
		}else if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireWriteConfig){
			function = "WRITE";
		}else if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireUpdateConfig){
			function = "UPDATE";
		}
		
		if("WRITE".equals(function)) {
			DesfireTag desfireTag =  desfireService.getDesfireTagToWrite();
			String desfireId = desfireTag.getTagWriteApi().getIdFromEppnInit(eppnInit);
			if(desfireId == null) {
				log.info("No Tag id selected for "  + eppnInit + " but it want to write one.");
				TagError tagError = new TagError();
				nfcResultBean.setCode(CODE.ERROR);
				nfcResultBean.setFullApdu(null);
				nfcResultBean.setMsg("Aucun individu sélectionné");		
				errorLongPoolController.handleError(tagError);
				return nfcResultBean;
			}
		}
		
		if("ERROR".equals(result)) {
			desfireService.reset();
			TagError tagError = new TagError();
			errorLongPoolController.handleError(tagError);
			nfcResultBean.setCode(CODE.ERROR);
			return nfcResultBean;			
		}
		
		if(result.length()==4){
			String msg = result.substring(2);
			DESFireEV1Service.Response response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			log.debug("Desfire Response is " + response.toString());
			if(!response.equals(DESFireEV1Service.Response.OPERATION_OK) && !response.equals(DESFireEV1Service.Response.ADDITIONAL_FRAME)) {
				desfireService.reset();
				TagError tagError = new TagError();
				errorLongPoolController.handleError(tagError);
				nfcResultBean.setCode(CODE.ERROR);
				return nfcResultBean;
			}
		}
			
		if("READ".equals(function)) {
			log.trace("READ");
			nfcResultBean = desfireService.readDesfireId(result);
		} else if("WRITE".equals(function)) {
			nfcResultBean = desfireService.writeCard(result, eppnInit);
			if(nfcResultBean.getAction().equals(Action.write)){
				write = true;
			}
		} else if("UPDATE".equals(function)) {
			nfcResultBean = desfireService.updateCard(result, cardId);
			if(nfcResultBean.getAction().equals(Action.update)){
				update = true;
			}
		}
		
		//authResultBean.setMsg(command);
		log.debug("next command for step " + desfireService.getStep() + " must be : " + nfcResultBean.getFullApdu());
		
		if("END".equals(nfcResultBean.getFullApdu())) {
			nfcResultBean.setCode(CODE.END);
			nfcResultBean.setCmd("");
			nfcResultBean.setParam("");
			nfcResultBean.setSize(0);
			nfcResultBean.setAction(Action.none);
			desfireService.reset();
			DESFireEV1Service.Response response = DESFireEV1Service.Response.UNKNOWN_CODE; 
			if(result != ""){
				String msg = result.substring(result.length() - 2);
				response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			}
			log.trace("Final response  : " + response);
			if(response.equals(DESFireEV1Service.Response.OPERATION_OK)){
				String desfireId = "";
				if("READ".equals(function)) {
					nfcResultBean.setAction(Action.read);
					log.trace("desfireId crypted  : " + result);
					desfireId = desfireService.decriptIDP2S(result);
					log.trace("desfireId descrypted  : " + desfireId);
				} else if ("WRITE".equals(function)) {
					DesfireTag desfireTag =  desfireService.getDesfireTagToWrite();
					desfireId = desfireTag.getTagWriteApi().getIdFromEppnInit(eppnInit);
					if(write) nfcResultBean.setAction(Action.write);
				} else if ("UPDATE".equals(function)) {
					DesfireTag desfireTag =  desfireService.getDesfireTagToUpdate();
					desfireId = desfireTag.getTagUpdateApi().getIdFromCsn(cardId);
					if(update) nfcResultBean.setAction(Action.update);
				}
				boolean validate = true;
				if(nfcResultBean.getAction().equals(Action.none)){
					validate = false;
				}
				TagLog tagLog = tagAuthService.auth(TagType.DESFIRE, desfireId, numeroId, cardId, validate);
				liveController.handleTagLog(tagLog);
				nfcResultBean.setMsg(tagLog.getFirstname() + " " + tagLog.getLastname());

			} else {
				TagError tagError = new TagError();
				errorLongPoolController.handleError(tagError);
				nfcResultBean.setCode(CODE.ERROR);
				nfcResultBean.setFullApdu(response.toString());
				nfcResultBean.setMsg("Aucun encodage");			
			}
		} else {
			String cmd = nfcResultBean.getFullApdu().substring(2, 4);
			nfcResultBean.setCmd(cmd);
			int nbParam = desfireService.desFireEV1Service.hexStringToByte(nfcResultBean.getFullApdu().substring(8, 10));
			nfcResultBean.setParam(nfcResultBean.getFullApdu().substring(10,10+(nbParam*2)));
		}

		nfcResultBean.setjSessionId(session.getId());
		log.trace(nfcResultBean.getMsg());
		return nfcResultBean;
	}
	
}
