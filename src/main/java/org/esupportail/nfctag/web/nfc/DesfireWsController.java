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

import org.esupportail.nfctag.domain.JsonResponseMessage;
import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.web.live.ErrorLongPoolController;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


@Controller
@RequestMapping("/desfire-ws")
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
	
	@ResponseBody
	public String hello() {
		return "hello";
	}
	
	/**
	 * phase 0
	 * @throws EsupNfcTagException 
	 */
	@RequestMapping(params="cmd=selectApp")
	@ResponseBody
	public String getSelectAppCommand(String numeroId) throws EsupNfcTagException {
		desfireService.setNumeroId(numeroId);
		return desfireService.getSelectAppCommand();
	}
	
	/**
	 * phase 1
	 */
	@RequestMapping(params="cmd=rndB") 
	@ResponseBody
	public String getRndbCommand() {
		return desfireService.getRndbCommand();
	}
	
	/**
	 * phase 2
	 */
	@RequestMapping(params="cmd=rndAPrimEnc")
	@ResponseBody
	public String getRndAPrimEncCommand(@RequestParam(required=true) String rndb) throws Exception {
		return desfireService.getRndAPrimEncCommand(rndb);
	}
	
	/**
	 * phase 3
	 */
	@RequestMapping(params="cmd=readFile")
	@ResponseBody
	public String getReadFileCommand() {
		return desfireService.getReadFileCommand();
	}
	
	/**
	 * phase 4
	 */
	@RequestMapping(params="cmd=desfireRequest")
	@ResponseBody
	public String desfireRequest(@RequestParam(required=true) String encDesfireId, @RequestParam(required=true) String rndAPrimEnc, @RequestParam(required=true) String numeroId) throws Exception {
		JsonResponseMessage jsonResponseMessage = new JsonResponseMessage();
		String desfireId = "";
		try {
			desfireId = desfireService.getDescryptedDesfireId(encDesfireId, rndAPrimEnc);
			log.debug("desfireId : " + desfireId);
			TagLog tagLog = tagAuthService.auth(TagType.DESFIRE, desfireId, numeroId);
			liveController.handleTagLog(tagLog);		
			jsonResponseMessage.setCode("OK");
			jsonResponseMessage.setMessage(tagLog.getFirstname() + " " + tagLog.getLastname());
		} catch(EsupNfcTagException e) {
			TagError tagError = new TagError(e);
			tagError.setNumeroId(numeroId);
			errorLongPoolController.handleError(tagError);
			jsonResponseMessage.setCode("ERROR");
			jsonResponseMessage.setMessage(e.getMessage());
			log.error("EsupNfcTagException during desfireRequest with desfireId = " + desfireId + " and numeroId=" + numeroId, e);
		} catch (Exception e) {
			TagError tagError = new TagError(e);
			errorLongPoolController.handleError(tagError);
			jsonResponseMessage.setCode("ERROR");
			jsonResponseMessage.setMessage(e.getMessage());
			log.error("Desfire tag issue", e);
		}
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(jsonResponseMessage);
		return json;

	}
	
}
