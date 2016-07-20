package org.esupportail.nfctag.web.nfc;

import java.io.IOException;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.JsonResponseMessage;
import org.esupportail.nfctag.domain.NfcMessage;
import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.web.live.ErrorLongPoolController;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RequestMapping("/csn-ws")
@Controller
@Transactional
public class CsnWsController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	TagAuthService tagAuthService;

	@Resource 
	LiveLongPoolController liveController;
	
	@Resource 
	ErrorLongPoolController errorLongPoolController;

	private JsonResponseMessage authCsn(String numeroId, String csn) {
		JsonResponseMessage jsonResponseMessage = new JsonResponseMessage();
		try {
			TagLog tagLog = tagAuthService.auth(TagType.CSN, csn, numeroId);
			liveController.handleTagLog(tagLog);		
			jsonResponseMessage.setCode("OK");
			jsonResponseMessage.setMessage(tagLog.getFirstname() + " " + tagLog.getLastname());
		} catch(EsupNfcTagException e) {
			TagError tagError = new TagError(e);
			tagError.setNumeroId(numeroId);
			errorLongPoolController.handleError(tagError);
			jsonResponseMessage.setCode("ERROR");
			jsonResponseMessage.setMessage(e.getMessage());
			log.error("EsupNfcTagException during csnRequest with csn = " + csn + " and numeroId=" + numeroId + " - "+ e.getMessage());
		}
		return jsonResponseMessage;
	}

	@RequestMapping(params = {"csn", "arduinoId"})
	@ResponseBody
	public String arduinoCsnRequest(String csn, String arduinoId) throws IOException {
		JsonResponseMessage jsonResponseMessage = authCsn(arduinoId, csn);
		String resp4arduino = "<";
		resp4arduino += jsonResponseMessage.getCode();
		resp4arduino += "\n";
		resp4arduino += "ERROR".equals(jsonResponseMessage.getCode()) ? "Erreur\n" : "";
		resp4arduino += jsonResponseMessage.getMessage();
		resp4arduino += "OK".equals(jsonResponseMessage.getCode()) ? "\nReconnu" : "";
		resp4arduino += ">";
		return resp4arduino;

	}


	@RequestMapping(method = RequestMethod.POST, headers = {"Content-type=application/json"}, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String csnRequest(@RequestBody NfcMessage nfcMessage) throws IOException {
		JsonResponseMessage jsonResponseMessage = authCsn(nfcMessage.getNumeroId(), nfcMessage.getCsn());
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(jsonResponseMessage);
		return json;
	}
	
}


