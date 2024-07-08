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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.esupportail.nfctag.domain.EsupNfcTagDridLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.transaction.annotation.Transactional;

@RequestMapping("/logs")
@Controller
@Transactional
public class NfcLogController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(method = RequestMethod.POST, headers = {"Content-type=application/json"})
	@ResponseBody
	public String logs(@RequestBody EsupNfcTagDridLog esupNfcTagDridLog) throws JsonProcessingException {
		
		String message = "READER ID : " + esupNfcTagDridLog.getNumeroId() + " - " + esupNfcTagDridLog.getErrorReport();
		String level = esupNfcTagDridLog.getErrorLevel();
		
		if("TRACE".equals(level)) {
			log.trace(message);
		} else if("DEBUG".equals(level)) {
			log.debug(message);	
		} else if("INFO".equals(level)) {
			log.info(message);		
		} else if("WARN".equals(level)) {
			log.warn(message);			
		} else if("ERROR".equals(level)) {
			log.error(message);
		}
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(esupNfcTagDridLog);

		return json;
	}
}



