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

import org.esupportail.nfctag.domain.JsonResponseMessage;
import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.KeyType;
import org.esupportail.nfctag.service.desfire.DesfireService;
//import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.web.live.ErrorLongPoolController;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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

	@RequestMapping("{step}")
	@ResponseBody
	public String[] process(String numeroId, @PathVariable("step") int step,  @RequestParam(required=true) String result,  HttpSession session) throws EsupNfcTagException, JsonProcessingException {
		desfireService.setNumeroId(numeroId);
		String[] next = {"", "error"};
		if(result.length()==4){
			String msg = result.substring(2);
			DESFireEV1Service.Response response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			//System.err.println(response);
			if(!response.equals(DESFireEV1Service.Response.OPERATION_OK)){
				TagError tagError = new TagError();
				errorLongPoolController.handleError(tagError);
				JsonResponseMessage jsonResponseMessage = new JsonResponseMessage();				
				jsonResponseMessage.setCode("ERROR");
				jsonResponseMessage.setMsg(response.toString());
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(jsonResponseMessage);				
				next[0] = json;
				next[1] = jsonResponseMessage.getCode();						
				return next;
			}
		}
			
		next[0] = desfireService.getIDP2sAES(step, result);
		if(next[0]==null){
			next[0]=desfireService.getIDP2sAES(step+1, result);
			next[1]=String.valueOf(step+2);
		}else{
			if(next[0].equals("OK")){
				next[1] = "OK";
			}else{
				next[1] = String.valueOf(step+1);
			}
		}
		
		if(next[0].equals("OK")){
			String msg = result.substring(result.length() - 2);
			DESFireEV1Service.Response response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			System.err.println(response);			
			if(response.equals(DESFireEV1Service.Response.OPERATION_OK)){
				TagLog tagLog = tagAuthService.auth(TagType.DESFIRE, desfireService.decriptIDP2S(result), numeroId);
				liveController.handleTagLog(tagLog);
				JsonResponseMessage jsonResponseMessage = new JsonResponseMessage();
				jsonResponseMessage.setCode("OK");
				jsonResponseMessage.setMsg(response.toString());
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(jsonResponseMessage);				
				next[0] = json;
				next[1] = jsonResponseMessage.getCode();	
			}else if(next[0].equals("ERROR")){
				TagError tagError = new TagError();
				errorLongPoolController.handleError(tagError);
				JsonResponseMessage jsonResponseMessage = new JsonResponseMessage();				
				jsonResponseMessage.setCode("ERROR");
				jsonResponseMessage.setMsg(response.toString());
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(jsonResponseMessage);				
				next[0] = json;
				next[1] = jsonResponseMessage.getCode();				
			}
		}
		
		return next;
	}
	
	@RequestMapping(params="cmd=end")
	@ResponseBody
	public String[] end(String numeroId, @RequestParam(required=true) String result,  HttpSession session) throws EsupNfcTagException {
		String msg = result.substring(2);
		DESFireEV1Service.Response response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
		System.err.println(response);
		String[] cmd = {"", "ERROR"};
		if(response.equals(DESFireEV1Service.Response.OPERATION_OK)){
			cmd[0] = "";
			cmd[1] = "OK";	
		}
		return cmd;
	}
	
	//exemple pour d'autres fonctions
	
/*
	@RequestMapping(params="cmd=init")
	@ResponseBody
	public String[] getInitCommand(String numeroId,  HttpSession session) throws EsupNfcTagException {
		session.invalidate();
		desFireEV1Service.reset();
		String[] cmd = {desFireEV1Service.getCardUID(),"createApplication"};
		return cmd;
	}

	@RequestMapping(params="cmd=selectApp")
	@ResponseBody
	public String[] getAppCommand(String numeroId) throws EsupNfcTagException {
		byte[] aid = desFireEV1Service.hexStringToByteArray(aidString);
		String[] cmd = {desFireEV1Service.selectApplication(desFireEV1Service.swapPairsByte(aid)),"authenticate1"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=selectWriteApp")
	@ResponseBody
	public String[] getselectWriteAppCommand(String numeroId) throws EsupNfcTagException {
		String aidString = "000001";
		byte[] aid = desFireEV1Service.hexStringToByteArray(aidString);
		String[] cmd = {desFireEV1Service.selectApplication(desFireEV1Service.swapPairsByte(aid)),"authenticate1"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=deleteApp")
	@ResponseBody
	public String[] getDeleteAppCommand(String numeroId) throws EsupNfcTagException {
		String aidString = "000001";
		byte[] aid = desFireEV1Service.hexStringToByteArray(aidString);
		String[] cmd = {desFireEV1Service.deleteApplication(desFireEV1Service.swapPairsByte(aid)),"OK"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=deleteFile")
	@ResponseBody
	public String[] deleteFile(String numeroId) throws Exception {
		String fileId = "04";
		String[] cmd = {desFireEV1Service.deleteFile(desFireEV1Service.hexStringToByte(fileId)),"OK"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=changeFileSettings")
	@ResponseBody
	public String[] changeFileSettings(String numeroId) throws Exception {
		String fileId = "01";
		String[] cmd = {desFireEV1Service.changeFileSettings(
				desFireEV1Service.hexStringToByte(fileId),
				(byte) 0xFF,
				(byte) 0xEE,
				(byte) 0xEE
				),"OK"};
		return cmd;
	}

	@RequestMapping(params="cmd=createApplication")
	@ResponseBody
	public String[] createApplication(String numeroId) throws Exception {
		String aidString = "000001";
		byte[] aid = desFireEV1Service.hexStringToByteArray(aidString);
		byte amks= (byte) 0x0B;
		byte nok = (byte) 0x84; //04 + 80 pour l'AES
		String[] cmd = {desFireEV1Service.createApplication(desFireEV1Service.swapPairsByte(aid),amks, nok),"OK"};
		return cmd;
	}	

	@RequestMapping(params="cmd=createDataFile")
	@ResponseBody
	public String[] getCreateDataFileCommand(String numeroId) throws EsupNfcTagException {
		String fileId = "01";
		byte[] payload = {desFireEV1Service.hexStringToByte(fileId), 0x01, (byte) 0xFF, (byte) 0xEE, 0x20, 0x00, 0x00};
		String[] cmd = {desFireEV1Service.createStdDataFile(payload),"OK"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=writeFile1")
	@ResponseBody
	public String[] getwriteFile1Command(@RequestParam(required=true) String result, String numeroId) throws EsupNfcTagException {
		System.err.println("write 1 result : " + result);
		String value = "6a 65 20 73 61 69 73 20 65 63 72 69 72 65 20 73 75 72 20 75 6e 65 20 63 61 72 74 65 20 21 00 00";
		String[] cmd = {desFireEV1Service.writeData1(value),"OK"};
		return cmd;
	}
	
	//pour les fichier longs
	@RequestMapping(params="cmd=writeFile2")
	@ResponseBody
	public String[] getwriteFile2Command(@RequestParam(required=true) String result, String numeroId) throws EsupNfcTagException {
		System.err.println("write 2 result : " + result);
		byte[] payload = {0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, (byte) 0xAA};
		String[] cmd = {desFireEV1Service.writeData2(payload),"OK"};
		return cmd;
	}

	@RequestMapping(params="cmd=readFile1")
	@ResponseBody
	public String[] getReadFile1Command(@RequestParam(required=true) String result, String numeroId) throws EsupNfcTagException {
		byte[] payload = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x00, (byte) 0x00};
		String[] cmd = {desFireEV1Service.readData(payload),"readFile2"};
		return cmd;
	}

	@RequestMapping(params="cmd=readFile2")
	@ResponseBody
	public String[] getReadFile2Command(@RequestParam(required=true) String result, String numeroId) throws EsupNfcTagException {

		byte[] resultByte = desFireEV1Service.hexStringToByteArray(result);
		System.err.println("dump result : "+Dump.hex(resultByte));
		byte[] resultDecript = desFireEV1Service.postprocess(resultByte, 22, DESFireEV1Service.CommunicationSetting.ENCIPHERED);

		System.err.println("decript result : " + desFireEV1Service.byteArrayToHexString(resultDecript));		

		String realDesfireId;
		try {
			realDesfireId = new String(resultDecript, "UTF-8");
			realDesfireId = realDesfireId.substring(1, 16);
			System.err.println(realDesfireId);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] cmd = {"9000","OK"};
		return cmd;
	}
	
	
	@RequestMapping(params="cmd=keySettings")
	@ResponseBody
	public String[] getKeySettingsCommand(String numeroId) throws EsupNfcTagException {
		String[] cmd = {desFireEV1Service.getKeySettings(),"OK"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=changeKeySettings")
	@ResponseBody
	public String[] getchangeKeySettingsCommand(String numeroId) throws EsupNfcTagException {
		byte keySett= (byte) 0x0F;
		String[] cmd = {desFireEV1Service.changeKeySettings(keySett),"OK"};
		return cmd;
	}
	
	@RequestMapping(params="cmd=changeKey")
	@ResponseBody
	public String[] getchangeKeyCommand(String numeroId, @RequestParam(required=true) String result) throws EsupNfcTagException {
		byte[] newKey = desFireEV1Service.hexStringToByteArray(keyComue);
		
		byte[] oldKey = desFireEV1Service.hexStringToByteArray(keyAES);
		
		String[] cmd = {desFireEV1Service.changeKey((byte) 0x00, (byte) 0x01, DESFireEV1Service.KeyType.AES, newKey, oldKey),"OK"};
		return cmd;
	}
*/
	
}
