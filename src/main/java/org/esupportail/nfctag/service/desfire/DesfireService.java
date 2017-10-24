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
package org.esupportail.nfctag.service.desfire;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.beans.DesfireApplication;
import org.esupportail.nfctag.beans.DesfireFile;
import org.esupportail.nfctag.beans.DesfireFlowStep;
import org.esupportail.nfctag.beans.DesfireFlowStep.Action;
import org.esupportail.nfctag.beans.DesfireKey;
import org.esupportail.nfctag.beans.DesfireTag;
import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.CODE;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.esupportail.nfctag.service.api.TagUpdateApi;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.api.impl.DesfireUpdateConfig;
import org.esupportail.nfctag.service.api.impl.DesfireWriteConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesfireService {
	
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private DesfireFlowStep desfireFlowStep = new DesfireFlowStep();
    
	private DesfireAuthSession desfireAuthSession;
	
	public DESFireEV1Service desFireEV1Service;

	private ApplicationsService applicationsService;
	
	private NfcAuthConfigService nfcAuthConfigService;

	public void setDesfireAuthSession(DesfireAuthSession desfireAuthSession) {
		this.desfireAuthSession = desfireAuthSession;
	}
	
	public void setDesFireEV1Service(DESFireEV1Service desFireEV1Service) {
		this.desFireEV1Service = desFireEV1Service;
	}
	
	public void setApplicationsService(ApplicationsService applicationsService) {
		this.applicationsService = applicationsService;
	}

	public void setNfcAuthConfigService(NfcAuthConfigService nfcAuthConfigService) {
		this.nfcAuthConfigService = nfcAuthConfigService;
	}
	
	public Action getStep() {
		return desfireFlowStep.action;
	}

	public NfcResultBean readDesfireId(String result){
		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.READ;
		}
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		DesfireReadConfig desfireReadConfig = (DesfireReadConfig) desfireAuthSession.getDesfireAuthConfig();
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getDesfireAppId());
		byte keyNo = desFireEV1Service.hexStringToByte(desfireReadConfig.getDesfireKeyNumber());
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getDesfireKey());
		byte[] payload = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getReadFilePayload());
		switch(desfireFlowStep.action){
			case READ:
				authResultBean = this.authApp(aid, result, key, keyNo, KeyType.AES);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.readData(payload));
					authResultBean.setSize(32);
					desfireFlowStep.action = Action.END;
				}
				break;
			case END:
				authResultBean.setFullApdu("END");
				break;
		}
		return authResultBean;
	}
	
	public DesfireTag getDesfireTagToWrite() {
		DesfireWriteConfig desfireWriteConfig = (DesfireWriteConfig) desfireAuthSession.getDesfireAuthConfig();		
		return desfireWriteConfig.getDesfireTag();
	}

	public DesfireTag getDesfireTagToUpdate() {
		DesfireUpdateConfig desfireUpdateConfig = (DesfireUpdateConfig) desfireAuthSession.getDesfireAuthConfig();		
		return desfireUpdateConfig.getDesfireTag();
	}
	
	public NfcResultBean writeCard(String result, String eppnInit){
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		
		DesfireTag desfireTag =  getDesfireTagToWrite();
		
		DesfireApplication desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireApp.getDesfireAppId());
		byte amks= desFireEV1Service.hexStringToByte(desfireApp.getAmks());
		byte nok = desFireEV1Service.hexStringToByte(desfireApp.getNok());
		
		DesfireKey desfireKey = desfireApp.getKeys().get(desfireFlowStep.currentKey);
		byte keyNo = desFireEV1Service.hexStringToByte(desfireKey.getKeyNo());
		byte[] defaultKey = desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000");
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireKey.getKey());
		byte keyVer = desFireEV1Service.hexStringToByte(desfireKey.getKeyVer());
		
		DesfireKey desfireAppKey = desfireApp.getKeys().get(0);
		byte[] appKey = desFireEV1Service.hexStringToByteArray(desfireAppKey.getKey());
		byte appKeyVer = desFireEV1Service.hexStringToByte(desfireAppKey.getKeyVer());
		
		DesfireFile desfireFile = desfireApp.getFiles().get(desfireFlowStep.currentFile);
		byte[] writepayload = desFireEV1Service.hexStringToByteArray(desfireFile.getWriteFilePayload());
		byte[] accessRights = desFireEV1Service.hexStringToByteArray(desfireFile.getAccessRights());
		byte cms = desFireEV1Service.hexStringToByte(desfireFile.getCommunicationSettings());
		byte fileNo = desFireEV1Service.hexStringToByte(desfireFile.getFileNumber());
		
		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.FORMAT;
		}
		switch(desfireFlowStep.action){
				case SELECT_ROOT:
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					desfireFlowStep.action = Action.CREATE_APP;
					break;
				case CREATE_APP: 
					authResultBean.setFullApdu(desFireEV1Service.createApplication(desFireEV1Service.swapPairsByte(aid),amks, nok));
					authResultBean.setSize(0);
					desfireFlowStep.action = Action.CHANGE_APP_KEY;
					break;
				case CHANGE_APP_KEY:
					authResultBean = this.authApp(aid, result, defaultKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, (byte) 0x00, DESFireEV1Service.KeyType.AES, appKey, defaultKey));
						authResultBean.setSize(16);					
						System.err.println("change key part");
						desfireFlowStep.action = Action.CHANGE_FILE_KEY;
						desfireFlowStep.authStep = 1;
					}
					break;
				case CHANGE_FILE_KEY:	
					authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, DESFireEV1Service.KeyType.AES, key, defaultKey));
						authResultBean.setSize(16);					
						System.err.println("change key part");
						desfireFlowStep.authStep = 1;				
						if(desfireFlowStep.currentKey+1<desfireApp.getKeys().size()) {
							desfireFlowStep.currentKey++;
						} else {
							desfireFlowStep.action = Action.CREATE_FILE;
							desfireFlowStep.currentKey = 1;
						}
					}
					break;
				case CREATE_FILE:	
					//apdu = this.authApp(result, key, (byte) 0x00);
					if(authResultBean.getFullApdu() == null) {
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(writepayload));
						authResultBean.setSize(16);					
						desfireFlowStep.action = Action.WRITE_FILE;
						desfireFlowStep.authStep = 1;
					} 
					desfireFlowStep.writeStep = 0;
					break;
				case WRITE_FILE:	
					if(desfireFlowStep.writeStep==0) {
						// authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES); - no need here
					}
					if(authResultBean.getFullApdu() == null || desfireFlowStep.writeStep>0) {
						if(desfireFlowStep.writeStep==0 || result.endsWith("AF")) {
							desfireFlowStep.authStep = 1;
							TagWriteApi tagWriteApi = desfireFile.getTagWriteApi();
							String desfireId = tagWriteApi.getIdFromEppnInit(eppnInit);
							authResultBean.setFullApdu(desFireEV1Service.writeData(fileNo, desfireId));
							authResultBean.setSize(16);						
							System.err.println("write part");
							desfireFlowStep.writeStep++;
						} else {
							desfireFlowStep.writeStep = 0;
							desfireFlowStep.action = Action.CHANGE_FILE_KEY_SET;
							authResultBean = writeCard(result, eppnInit);
						} 
					} 
					break;
				case CHANGE_FILE_KEY_SET:	
					authResultBean.setFullApdu(desFireEV1Service.changeFileSettings(fileNo, cms, accessRights[1], accessRights[0]));
					authResultBean.setSize(16);					
					System.err.println("change key set part");
					if(desfireFlowStep.currentFile+1<desfireApp.getFiles().size()) {
						desfireFlowStep.currentFile++;
						desfireFlowStep.action = Action.CREATE_FILE;
					} else {
						desfireFlowStep.currentFile = 0;
						if(desfireFlowStep.currentApp+1<desfireTag.getApplications().size()) {
							desfireFlowStep.currentApp++;
							desfireFlowStep.action = Action.SELECT_ROOT;
						} else {
							desfireFlowStep.currentApp = 0;                                         
						    desfireFlowStep.action = Action.END;
						}
					}
					break;
				case FORMAT:
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, desFireEV1Service.hexStringToByteArray("0000000000000000"), (byte) 0x00, KeyType.DES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.formatPICC());
						authResultBean.setSize(16);					
						System.err.println("flush part");
						desfireFlowStep.action = Action.CREATE_APP;
						desfireFlowStep.authStep = 1;
					}
					break;
				case END:	
					authResultBean.setFullApdu("END");
					break;
			}
		return authResultBean;
	}

	public NfcResultBean updateCard(String result, String cardId){
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.none);
		authResultBean.setCode(CODE.OK);
		
		DesfireTag desfireTag = getDesfireTagToUpdate();
		
		DesfireApplication desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireApp.getDesfireAppId());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date schemaDate = null;
		try {
			schemaDate = formatter.parse(desfireApp.getUpdateDate());
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		
		//System.err.println("schemaDate : " + updateDate);
		byte amks= desFireEV1Service.hexStringToByte(desfireApp.getAmks());
		byte nok = desFireEV1Service.hexStringToByte(desfireApp.getNok());
		
		DesfireKey desfireKey = desfireApp.getKeys().get(desfireFlowStep.currentKey);
		byte keyNo = desFireEV1Service.hexStringToByte(desfireKey.getKeyNo());
		byte[] defaultKey = desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000");
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireKey.getKey());
		byte keyVer = desFireEV1Service.hexStringToByte(desfireKey.getKeyVer());
		
		DesfireKey desfireAppKey = desfireApp.getKeys().get(0);
		byte[] appKey = desFireEV1Service.hexStringToByteArray(desfireAppKey.getKey());
		byte appKeyVer = desFireEV1Service.hexStringToByte(desfireAppKey.getKeyVer());
		
		DesfireFile desfireFile = desfireApp.getFiles().get(desfireFlowStep.currentFile);
		byte[] writepayload = desFireEV1Service.hexStringToByteArray(desfireFile.getWriteFilePayload());
		byte[] accessRights = desFireEV1Service.hexStringToByteArray(desfireFile.getAccessRights());
		byte cms = desFireEV1Service.hexStringToByte(desfireFile.getCommunicationSettings());
		byte fileNo = desFireEV1Service.hexStringToByte(desfireFile.getFileNumber());
		
		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.SELECT_ROOT;
		}
		switch(desfireFlowStep.action){
				case SELECT_ROOT:
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					desfireFlowStep.action = Action.GET_APPS;
					break;
				case GET_APPS:
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, desFireEV1Service.hexStringToByteArray("0000000000000000"), (byte) 0x00, KeyType.DES);
 					if(authResultBean.getFullApdu() == null) {
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.getApplicationsIds1());
						authResultBean.setSize(16);
						desfireFlowStep.action = Action.CHECK_APP;
 					}
					break;
				case CHECK_APP: 
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					String appsString = result.substring(0, result.length()-4);
					// split result : app id on 6 chars (3 bytes) 
					List<String> apps = Arrays.asList(appsString.split("(?<=\\G.{6})"));
					
					TagUpdateApi appTagUpdateApi = desfireApp.getTagUpdateApi();
					Date lastUpdate = appTagUpdateApi.getLastUpdateDateFromCsn(cardId);
					if(desfireFlowStep.currentApp < desfireTag.getApplications().size()) {
						System.err.println("schemaDate : " + schemaDate);
						System.err.println("lastUpdate : " + lastUpdate);
						if(schemaDate!=null && lastUpdate.before(schemaDate)){
							authResultBean.setAction(NfcResultBean.Action.update);
							if(!apps.contains(desFireEV1Service.byteArrayToHexString(desFireEV1Service.swapPairsByte(aid)))) {
								desfireFlowStep.action = Action.CREATE_APP;
							}else{
								desfireFlowStep.action = Action.DELETE_APP;
							}
						}else{
							if(desfireFlowStep.currentApp + 1 < desfireTag.getApplications().size()) {
								desfireFlowStep.currentApp++;
								desfireFlowStep.action = Action.SELECT_ROOT;
							}else{
								desfireFlowStep.currentApp = 0;
								desfireFlowStep.action = Action.END;
							}
						}
					}
					break;	
				case DELETE_APP: 
					authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.deleteApplication(desFireEV1Service.swapPairsByte(aid)));
						authResultBean.setSize(0);						
						desfireFlowStep.action = Action.CREATE_APP;
					}
					break;
				case CREATE_APP: 
					authResultBean.setFullApdu(desFireEV1Service.createApplication(desFireEV1Service.swapPairsByte(aid),amks, nok));
					authResultBean.setSize(0);
					desfireFlowStep.action = Action.CHANGE_APP_KEY;
					break;
				case CHANGE_APP_KEY:
					authResultBean = this.authApp(aid, result, defaultKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, (byte) 0x00, DESFireEV1Service.KeyType.AES, appKey, defaultKey));
						authResultBean.setSize(16);					
						System.err.println("change key part");
						desfireFlowStep.action = Action.CHANGE_FILE_KEY;
						desfireFlowStep.authStep = 1;
					}
					break;
				case CHANGE_FILE_KEY:	
					authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, DESFireEV1Service.KeyType.AES, key, defaultKey));
						authResultBean.setSize(16);					
						System.err.println("change key part");
						desfireFlowStep.authStep = 1;				
						if(desfireFlowStep.currentKey + 1 < desfireApp.getKeys().size()) {
							desfireFlowStep.currentKey++;
						} else {
							desfireFlowStep.action = Action.CREATE_FILE;
							desfireFlowStep.currentKey = 1;
						}
					}
					break;
				case CREATE_FILE:	
					//apdu = this.authApp(result, key, (byte) 0x00);
					if(authResultBean.getFullApdu() == null) {
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(writepayload));
						authResultBean.setSize(16);					
						desfireFlowStep.action = Action.WRITE_FILE;
						desfireFlowStep.authStep = 1;
					} 
					desfireFlowStep.writeStep = 0;
					break;
				case WRITE_FILE:	
					if(desfireFlowStep.writeStep==0) {
						// authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES); - no need here
					}
					if(authResultBean.getFullApdu() == null || desfireFlowStep.writeStep>0) {
						if(desfireFlowStep.writeStep==0 || result.endsWith("AF")) {
							desfireFlowStep.authStep = 1;
							TagUpdateApi fileTagUpdateApi = desfireFile.getTagUpdateApi();
							String desfireId = fileTagUpdateApi.getIdFromCsn(cardId);
							authResultBean.setFullApdu(desFireEV1Service.writeData(fileNo, desfireId));
							authResultBean.setSize(16);						
							System.err.println("write part");
							desfireFlowStep.writeStep++;
						} else {
							desfireFlowStep.writeStep = 0;
							desfireFlowStep.action = Action.CHANGE_FILE_KEY_SET;
							authResultBean = updateCard(result, cardId);
						} 
					} 
					break;
				case CHANGE_FILE_KEY_SET:	
					authResultBean.setFullApdu(desFireEV1Service.changeFileSettings(fileNo, cms, accessRights[1], accessRights[0]));
					authResultBean.setSize(16);					
					System.err.println("change key set part");
					if(desfireFlowStep.currentFile + 1 < desfireApp.getFiles().size()) {
						desfireFlowStep.currentFile++;
						desfireFlowStep.action = Action.CREATE_FILE;
					} else {
						desfireFlowStep.currentFile = 0;
						if(desfireFlowStep.currentApp + 1 < desfireTag.getApplications().size()) {
							desfireFlowStep.currentApp++;
							desfireFlowStep.action = Action.SELECT_ROOT;
						} else {
							desfireFlowStep.currentApp = 0;                                         
						    desfireFlowStep.action = Action.END;
						}
					}
					break;
				case END:	
					authResultBean.setFullApdu("END");
					break;
			}
		return authResultBean;
	}
	
	private NfcResultBean authApp(byte[] aid, String result, byte[] key, byte keyNo, KeyType keyType) {
		if(result.length()==0){
			desfireFlowStep.authStep = 1;
		}
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.auth);
		authResultBean.setCode(CODE.OK);
		switch(desfireFlowStep.authStep){
			case 1:
				authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.swapPairsByte(aid)));
				authResultBean.setSize(0);
				desfireFlowStep.authStep++;
				break;
			case 2:
				authResultBean.setFullApdu(desFireEV1Service.authenticate1(key,keyNo, keyType));
				authResultBean.setSize(16);
				desfireFlowStep.authStep++;
				break;
			case 3:
				authResultBean.setFullApdu(desFireEV1Service.authenticate2(key, keyNo,keyType,desFireEV1Service.hexStringToByteArray(result)));
				authResultBean.setSize(16);
				desfireFlowStep.authStep++;
				break;
			case 4:
				authResultBean.setFullApdu(desFireEV1Service.authenticate3(key, keyNo, keyType, desFireEV1Service.hexStringToByteArray(result)));
				authResultBean.setSize(0);
				break;
			}
		return authResultBean;
	}
	
	
	public String decriptIDP2S(String apdu){
		byte[] resultByte = desFireEV1Service.hexStringToByteArray(apdu);
		DesfireReadConfig desfireReadConfig = (DesfireReadConfig) desfireAuthSession.getDesfireAuthConfig();
		String fileSize = desfireReadConfig.getDesfireFileSize();
		byte[] fileSizeByte = desFireEV1Service.swapPairsByte(desFireEV1Service.hexStringToByteArray(fileSize));
		int length = 0;
		int pow = 1;
		for(int i = 2 ; i>=0; i--) {
			length += Math.pow(fileSizeByte[i], pow);
			pow = pow*256;
		}
		System.err.println("length : " + length);
		byte[] resultDecript = desFireEV1Service.postprocess(resultByte, length, DESFireEV1Service.CommunicationSetting.ENCIPHERED);
		String hexaResult =  desFireEV1Service.byteArrayToHexString(resultDecript);
		System.err.println("En hexa : " + hexaResult);
		return hexaResult;
	}
	

	public String asBytesString(String desfireId) {		
		int size = (int)(desfireId.length()/16)+1;
		desfireId = StringUtils.leftPad(desfireId, size*16, '0');	
		String desfireIdAsBytesString = desFireEV1Service.byteArrayToHexString(desfireId.getBytes());
		System.err.println("desfireIdAsBytesString : " + desfireIdAsBytesString);
		return desfireIdAsBytesString;
	}
	
	public void setNumeroId(String numeroId) throws EsupNfcTagException {
		String nfcAuthConfigKey = applicationsService.getApplicationFromNumeroId(numeroId).getNfcConfig();
		NfcAuthConfig desfireAuthConfig = (NfcAuthConfig) nfcAuthConfigService.get(nfcAuthConfigKey);
		desfireAuthSession.setDesfireAuthConfig(desfireAuthConfig);
	}

	public void reset() {
		desfireFlowStep = new DesfireFlowStep();
	}
    
	
	public NfcResultBean unBrick(String result){

		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		
		if(result.length()==0){
			desfireFlowStep.action = Action.UNBRICK;
		}
		switch(desfireFlowStep.action){
				case UNBRICK:	
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, desFireEV1Service.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF"), (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						System.err.println("change key part");						
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, (byte) 0x00, DESFireEV1Service.KeyType.DES, desFireEV1Service.hexStringToByteArray("0000000000000000"), desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000")));
						authResultBean.setSize(16);					
						desfireFlowStep.authStep = 1;				
							desfireFlowStep.action = Action.END;
							desfireFlowStep.currentKey = 0;
					}
					break;
				case END:	
					authResultBean.setFullApdu("END");
					break;
			}
		return authResultBean;
	}
	
	public NfcResultBean switchToAES(String result){

		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		
		if(result.length()==0){
			desfireFlowStep.action = Action.SWITCH_TO_AES;
		}
		switch(desfireFlowStep.action){
				case SWITCH_TO_AES:	
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, desFireEV1Service.hexStringToByteArray("0000000000000000"), (byte) 0x00, KeyType.DES);
					if(authResultBean.getFullApdu() == null) {
						System.err.println("change key part");						
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, (byte) 0x01, DESFireEV1Service.KeyType.AES, desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000"), desFireEV1Service.hexStringToByteArray("0000000000000000")));
						System.err.println(authResultBean.getFullApdu());
						authResultBean.setSize(16);					
		
						desfireFlowStep.authStep = 1;				
						desfireFlowStep.action = Action.END;
						desfireFlowStep.currentKey = 0;
					}
					break;
				case END:	
					authResultBean.setFullApdu("END");
					break;
			}
		return authResultBean;
	}
	
}
