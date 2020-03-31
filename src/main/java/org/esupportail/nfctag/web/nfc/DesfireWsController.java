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
import org.esupportail.nfctag.beans.NfcResultBean.Action;
import org.esupportail.nfctag.beans.NfcResultBean.CODE;
import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.api.impl.DesfireReadUidConfig;
import org.esupportail.nfctag.service.api.impl.DesfireReadUidWithAuthConfig;
import org.esupportail.nfctag.service.api.impl.DesfireUpdateConfig;
import org.esupportail.nfctag.service.api.impl.DesfireWriteConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service;
import org.esupportail.nfctag.service.desfire.DesfireAuthSession;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.DesfireUtils;
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
    Boolean update = false;
    Boolean write = false;
    String function;
    
	@RequestMapping(produces = "application/json")
	@ResponseBody
	public NfcResultBean process(@RequestParam String numeroId, @RequestParam String cardId, @RequestParam String result, HttpSession session) {

		NfcResultBean nfcResultBean = new NfcResultBean();
		
		if(!applicationsService.checkApplicationFromNumeroId(numeroId)){
			nfcResultBean.setCode(CODE.ERROR);
			nfcResultBean.setMsg("device config error");
			log.error("device error for " + numeroId + " please check configuration");
			return nfcResultBean;
		}
		
		if(desfireService.getStep()==null) {
			update = false;
			write = false;
			eppnInit = tagAuthService.getEppnInit(TagType.DESFIRE, numeroId);
			desfireService.setNumeroId(numeroId);
			if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireReadUidConfig){
				function = "READUID";
			}else if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireReadUidWithAuthConfig) {
				function = "READUIDWITHAUTH";
			}else if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireReadConfig){
				function = "READ";
			}else if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireWriteConfig){
				function = "WRITE";
			}else if(desfireAuthSession.getDesfireAuthConfig() instanceof DesfireUpdateConfig){
				function = "UPDATE";
			}
			log.debug("INIT " + function + " on cardId : " + cardId + " by " + eppnInit);
		}else{
	
		}
	
		if("ERROR".equals(result)) {
			desfireService.reset();
			TagError tagError = new TagError();
			errorLongPoolController.handleError(tagError);
			nfcResultBean.setCode(CODE.ERROR);
			return nfcResultBean;			
		}
		DESFireEV1Service.Response response = DESFireEV1Service.Response.UNKNOWN_CODE;
		
		if(result.length() > 2 ){
			String msg = result.substring(result.length() - 2);
			response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			log.debug("return is : " + result + " with Desfire Response : " + response.toString());
			if(!response.equals(DESFireEV1Service.Response.OPERATION_OK) && !response.equals(DESFireEV1Service.Response.ADDITIONAL_FRAME)) {
				desfireService.reset();
				TagError tagError = new TagError();
				errorLongPoolController.handleError(tagError);
				nfcResultBean.setCode(CODE.ERROR);
				return nfcResultBean;
			}
		}
		
		try{	
			if("READUID".equals(function)) {
				log.trace("READUID");
				nfcResultBean = desfireService.readUid(result);
			} else if("READUIDWITHAUTH".equals(function)) {
				log.trace("READUIDWITHAUTH");
				nfcResultBean = desfireService.readUidWithAuth(result);
			} else if("READ".equals(function)) {
				log.trace("READ");
				nfcResultBean = desfireService.readDesfireId(result);
			} else if("WRITE".equals(function)) {
				nfcResultBean = desfireService.writeCard(result, eppnInit, cardId);
				if(nfcResultBean.getAction().equals(Action.write)){
					write = true;
				}
			} else if("UPDATE".equals(function)) {
				nfcResultBean = desfireService.updateCard(result, eppnInit, cardId);
				if(nfcResultBean.getAction().equals(Action.update)){
					update = true;
				}
			}
		} catch (EsupNfcTagException e) {
			desfireService.reset();
			TagError tagError = new TagError();
			errorLongPoolController.handleError(tagError);
			nfcResultBean.setCode(CODE.ERROR);
			return nfcResultBean;	
		}
		
		if("END".equals(nfcResultBean.getFullApdu())) {
			
			nfcResultBean.setCode(CODE.END);
			nfcResultBean.setCmd("");
			nfcResultBean.setParam("");
			nfcResultBean.setSize(0);
			nfcResultBean.setAction(Action.none);
			desfireService.reset();
			if("READ".equals(function) || "READUID".equals(function)) {
				result = desfireService.tempRead;
			}
			if(result != ""){
				String msg = result.substring(result.length() - 2);
				response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			}
			if(response.equals(DESFireEV1Service.Response.OPERATION_OK)){
				TagType tagType = TagType.CSN;
				String desfireId = "";
				String appName = "";
				boolean validate = true;
				if("READUID".equals(function)){
					DesfireReadUidConfig desfireReadConfig = (DesfireReadUidConfig) desfireAuthSession.getDesfireAuthConfig();
					appName = desfireReadConfig.getDesfireAppName();
					nfcResultBean.setAction(Action.read);
					log.debug("UID  : " + result);
					desfireId = result.substring(28, 28 + 14);
					log.debug("UID descrypted  : " + desfireId);
					tagType = TagType.CSN;
				} else if("READUIDWITHAUTH".equals(function)){
					DesfireReadUidWithAuthConfig desfireReadConfig = (DesfireReadUidWithAuthConfig) desfireAuthSession.getDesfireAuthConfig();
					appName = desfireReadConfig.getDesfireAppName();
					nfcResultBean.setAction(Action.read);
					log.debug("UID  : " + result);
					desfireId = desfireService.decriptUid(result);
					log.debug("UID descrypted  : " + desfireId);
					tagType = TagType.CSN;
				} else if("READ".equals(function)) {
					DesfireReadConfig desfireReadConfig = (DesfireReadConfig) desfireAuthSession.getDesfireAuthConfig();
					appName = desfireReadConfig.getDesfireAppName();
					nfcResultBean.setAction(Action.read);
					log.debug("desfireId crypted  : " + result);
					desfireId = desfireService.decriptDesfireId(result);
					log.debug("desfireId descrypted  : " + desfireId);
					tagType = TagType.DESFIRE;
				} else if ("WRITE".equals(function)) {
					if(write) {
						nfcResultBean.setAction(Action.write);
					}
				} else if ("UPDATE".equals(function)) {
					if(update) {
						nfcResultBean.setAction(Action.update);
					}
				}
				if(nfcResultBean.getAction().equals(Action.none)){
					validate = false;
				}
				
				try {
					TagLog tagLog = tagAuthService.auth(tagType, desfireId, numeroId, cardId, appName, validate);
					liveController.handleTagLog(tagLog);
					nfcResultBean.setMsg(tagLog.getFirstname() + " " + tagLog.getLastname());
					nfcResultBean.setTaglogId(tagLog.getId());
				}catch (EsupNfcTagException e){
					log.error("auth exception : " + e.getMessage());
					TagError tagError = new TagError(e);
					tagError.setNumeroId(numeroId);
					errorLongPoolController.handleError(tagError);
					nfcResultBean.setCode(CODE.ERROR);
					nfcResultBean.setMsg(e.getMessage());
				}

			} else {
				TagError tagError = new TagError(new Exception(response.toString()));
				log.error("desfire error : " + tagError.getException().getMessage());
				errorLongPoolController.handleError(tagError);
				nfcResultBean.setCode(CODE.ERROR);
				nfcResultBean.setFullApdu(response.toString());
				nfcResultBean.setMsg("Aucun encodage");			
			}
		} else {
			String cmd = nfcResultBean.getFullApdu().substring(2, 4);
			nfcResultBean.setCmd(cmd);
			int nbParam = DesfireUtils.hexStringToByte(nfcResultBean.getFullApdu().substring(8, 10));
			nfcResultBean.setParam(nfcResultBean.getFullApdu().substring(10,10+(nbParam*2)));
		}

		nfcResultBean.setjSessionId(session.getId());
		log.trace(nfcResultBean.getMsg());
		log.debug("command for card must be : " + nfcResultBean.getFullApdu());
		log.debug("next step will be : " + desfireService.getStep() + " with auth step : " + desfireService.getAuthStep());
		return nfcResultBean;
	}
	
}
