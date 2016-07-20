package org.esupportail.nfctag.web.nfc;

import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.EsupNfcTagDridLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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



