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
	
	public String tempRead = "";
	public String tempFileSize = "";

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
			desfireFlowStep.action = Action.GET_FILE_SETTINGS;
		}
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		DesfireReadConfig desfireReadConfig = (DesfireReadConfig) desfireAuthSession.getDesfireAuthConfig();
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getDesfireAppId());
		byte keyNo = desFireEV1Service.hexStringToByte(desfireReadConfig.getDesfireKeyNumber());
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getDesfireKey());
		byte fileNo = desFireEV1Service.hexStringToByte(desfireReadConfig.getDesfireFileNumber());
		byte[] offset = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getDesfireFileOffset());
		byte[] payload = desFireEV1Service.hexStringToByteArray(desfireReadConfig.getReadFilePayload());
		
		switch(desfireFlowStep.action){
			case GET_FILE_SETTINGS:
				tempFileSize = "";
				authResultBean = this.authApp(aid, result, key, keyNo, KeyType.AES);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.getFileSettings(fileNo));
					desfireFlowStep.action = Action.READ;
				}
				break;
			case READ:
				tempRead = "";
				if(tempFileSize.equals("")){
					tempFileSize = result.substring(8, 14);
				}
				byte[] length = desFireEV1Service.hexStringToByteArray(tempFileSize);
				authResultBean = this.authApp(aid, result, key, keyNo, KeyType.AES);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.readData(fileNo, offset, length));
					authResultBean.setSize(32);
					desfireFlowStep.action = Action.MORE;
				}
				break;
			case MORE:
				if(result.endsWith("AF")){
					tempRead += result.substring(0, result.length() - 4);
					authResultBean.setFullApdu(desFireEV1Service.readMore());
					authResultBean.setSize(32);
					desfireFlowStep.action = Action.MORE;
				}else if(result.endsWith("9100")){
					tempRead += result;
					authResultBean.setFullApdu("END");
				}
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
	
	public NfcResultBean writeCard(String result, String eppnInit, String csn){
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.none);
		authResultBean.setCode(CODE.OK);
		
		byte[] defaultKey;
		DesfireTag desfireTag =  getDesfireTagToWrite();
		if(desfireTag.getApplications() == null){
			if(result.length()==0){
				desfireFlowStep.action = Action.SELECT_ROOT;
			}
			switch(desfireFlowStep.action){
				case SELECT_ROOT:
					log.debug("Nothing  by " + eppnInit + " - Select root");
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					desfireFlowStep.action = Action.END;
					break;
				case END:
					authResultBean.setFullApdu("END");
					authResultBean.setAction(NfcResultBean.Action.write);
					break;
			}
			return authResultBean;
		}
		
		byte[] piccKeyStart = desFireEV1Service.hexStringToByteArray(desfireTag.getKeyStart());
		KeyType piccKeyTypeStart = desfireTag.getKeyTypeStart();
		byte piccKeyVerStart = desFireEV1Service.hexStringToByte(desfireTag.getKeyVersionStart());
		
		byte[] piccKeyFinish = desFireEV1Service.hexStringToByteArray(desfireTag.getKeyFinish());
		KeyType piccKeyTypeFinish = desfireTag.getKeyTypeFinish();
		byte piccKeyVerFinish = desFireEV1Service.hexStringToByte(desfireTag.getKeyVersionFinish());
		
		DesfireApplication desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireApp.getDesfireAppId());
		byte amks= desFireEV1Service.hexStringToByte(desfireApp.getAmks());
		byte nok = desFireEV1Service.hexStringToByte(desfireApp.getNok());
		
		DesfireKey desfireAppKey = desfireApp.getKeys().get(0);
		byte[] appKey = desFireEV1Service.hexStringToByteArray(desfireAppKey.getKey());
		byte appKeyNo = desFireEV1Service.hexStringToByte(desfireAppKey.getKeyNo());
		byte appKeyVer = desFireEV1Service.hexStringToByte(desfireAppKey.getKeyVer());
		KeyType appKeyType = desfireAppKey.getType();
		KeyType appDefaultKeyType;
		if(desfireApp.getNok().substring(0, 1).equals("8")){
			appDefaultKeyType = KeyType.AES;
			defaultKey = desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000");
		}else{
			appDefaultKeyType = KeyType.DES;
			defaultKey = desFireEV1Service.hexStringToByteArray("0000000000000000");
		}
		
		
		DesfireKey desfireKey = desfireApp.getKeys().get(desfireFlowStep.currentKey);
		byte keyNo = desFireEV1Service.hexStringToByte(desfireKey.getKeyNo());
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireKey.getKey());
		byte keyVer = desFireEV1Service.hexStringToByte(desfireKey.getKeyVer());
		KeyType keyKeyType = desfireKey.getType();
		
		DesfireFile desfireFile = desfireApp.getFiles().get(desfireFlowStep.currentFile);
		byte[] accessRights = desFireEV1Service.hexStringToByteArray(desfireFile.getAccessRights());
		byte cms = desFireEV1Service.hexStringToByte(desfireFile.getCommunicationSettings());
		byte fileNo = desFireEV1Service.hexStringToByte(desfireFile.getFileNumber());
		
		TagWriteApi tagWriteApi = desfireFile.getTagWriteApi();
		String desfireId = tagWriteApi.getIdFromCsn(csn);
		String writePayload;
		if(desfireFile.getFileSize() != null){
			writePayload = desfireFile.getWriteFilePayload() + desfireFile.getFileSize();
		} else{
			//calcul automatique de la taille du fichier en fonction du contenu
			int fileSize = desfireId.length() / 2;
			String hexSize = leftPad(Integer.toHexString(fileSize), 6, "0".toCharArray()[0]);
			writePayload = desfireFile.getWriteFilePayload() + desFireEV1Service.swapPairs(desFireEV1Service.hexStringToByteArray(hexSize));
			
		}
		byte[] byteWritePayload = desFireEV1Service.hexStringToByteArray(writePayload);
		
		if(result.length()==0){
			reset();
			if(desfireTag.getFormatBeforeWrite()){
				desfireFlowStep.action = Action.FORMAT;
			}else{
				desfireFlowStep.action = Action.SELECT_ROOT;
			}
		}
		switch(desfireFlowStep.action){
				case FORMAT:
					log.debug("Write by " + eppnInit + " - Format card");
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.formatPICC());
						authResultBean.setSize(16);					
						log.trace(eppnInit + " - Format card");
						desfireFlowStep.action = Action.CREATE_APP;
						desfireFlowStep.authStep = 1;
					}
					break;
				case SELECT_ROOT:
					log.debug("Write by " + eppnInit + " - Select root");
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
						authResultBean.setSize(16);
						log.trace(eppnInit + " - Select ROOT APP");
						desfireFlowStep.action = Action.CREATE_APP;
						desfireFlowStep.authStep = 1;
					}
					break;
				case CREATE_APP: 
					log.debug("Write by " + eppnInit + " - Create app : " + aid);
					authResultBean.setFullApdu(desFireEV1Service.createApplication(desFireEV1Service.swapPairsByte(aid),amks, nok));
					authResultBean.setSize(0);
					desfireFlowStep.action = Action.CHANGE_APP_MASTER_KEY;
					break;
				case CHANGE_APP_MASTER_KEY:
					log.debug("Write by " + eppnInit + " - Change app key : " + desFireEV1Service.byteArrayToHexString(aid));
					authResultBean = this.authApp(aid, result, defaultKey, appKeyNo, appDefaultKeyType);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey(appKeyNo, appKeyVer, appKeyType, appKey, defaultKey));
						authResultBean.setSize(16);					
						log.trace("change key part");
						desfireFlowStep.action = Action.CHANGE_KEYS;
						desfireFlowStep.authStep = 1;
					}
					break;
				case CHANGE_KEYS:
					log.debug("Write by " + eppnInit + " - Change app key : " + keyNo + " for " + desFireEV1Service.byteArrayToHexString(aid));
					authResultBean = this.authApp(aid, result, appKey, appKeyNo, appDefaultKeyType);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, keyKeyType, key, defaultKey));
						authResultBean.setSize(16);					
						log.trace("change key part");
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
					if(authResultBean.getFullApdu() == null) {
						log.debug("Write by " + eppnInit + " - Create file in " + desFireEV1Service.byteArrayToHexString(aid) +" : " + fileNo);
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(byteWritePayload));
						authResultBean.setSize(16);					
						desfireFlowStep.action = Action.WRITE_FILE;
						desfireFlowStep.authStep = 1;
					} 
					desfireFlowStep.writeStep = 0;
					break;
				case WRITE_FILE:	
					if(authResultBean.getFullApdu() == null || desfireFlowStep.writeStep>0) {
						if(desfireFlowStep.writeStep==0) {
							desfireFlowStep.authStep = 1;
							log.debug("Write by " + eppnInit + " - Write in file " + fileNo +" : " + desfireId);
							authResultBean.setFullApdu(desFireEV1Service.writeData(fileNo, desfireId));
							authResultBean.setSize(16);						
							desfireFlowStep.writeStep++;
						}else if(desfireFlowStep.writeStep > 0 && result.endsWith("AF")){
							authResultBean.setFullApdu(desFireEV1Service.writeMore(desfireId));
							authResultBean.setSize(16);							
						} else {
							desfireFlowStep.writeStep = 0;
							desfireFlowStep.action = Action.CHANGE_FILE_KEY_SET;
							authResultBean = writeCard(result, eppnInit, csn);
						} 
					} 
					break;
				case CHANGE_FILE_KEY_SET:	
					log.debug("Write by " + eppnInit + " - Change key settings " + fileNo +" : " + cms + ", " + accessRights[1] +", "+accessRights[0]);
					authResultBean.setFullApdu(desFireEV1Service.changeFileSettings(fileNo, cms, accessRights[1], accessRights[0]));
					authResultBean.setSize(16);					
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
						    desfireFlowStep.action = Action.CHANGE_PICC_KEY;
						}
					}
					break;
				case CHANGE_PICC_KEY:
					log.debug("Write by " + eppnInit + " - PICC master key");
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, piccKeyVerFinish, piccKeyTypeFinish, piccKeyFinish, piccKeyStart));
						authResultBean.setSize(16);					
						desfireFlowStep.authStep = 1;				
						desfireFlowStep.action = Action.END;
						desfireFlowStep.currentKey = 0;
					}
					break;
				case END:
					authResultBean.setAction(NfcResultBean.Action.write);
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

		if(desfireTag.getApplications() == null){
			if(result.length()==0){
				desfireFlowStep.action = Action.SELECT_ROOT;
			}
			switch(desfireFlowStep.action){
				case SELECT_ROOT:
					log.debug("Nothing  by " + cardId + " - Select root");
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					desfireFlowStep.action = Action.END;
					break;
				case END:
					authResultBean.setFullApdu("END");
					authResultBean.setAction(NfcResultBean.Action.none);
					break;
			}
			return authResultBean;
		}
		
		byte[] piccKeyStart = desFireEV1Service.hexStringToByteArray(desfireTag.getKeyStart());
		KeyType piccKeyTypeStart = desfireTag.getKeyTypeStart();
		byte piccKeyVerStart = desFireEV1Service.hexStringToByte(desfireTag.getKeyVersionStart());

		byte[] piccKeyFinish = desFireEV1Service.hexStringToByteArray(desfireTag.getKeyFinish());
		KeyType piccKeyTypeFinish = desfireTag.getKeyTypeFinish();
		byte piccKeyVerFinish = desFireEV1Service.hexStringToByte(desfireTag.getKeyVersionFinish());
		
		DesfireApplication desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireApp.getDesfireAppId());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date schemaDate = null;
		try {
			schemaDate = formatter.parse(desfireApp.getUpdateDate());
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		
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
		KeyType appKeyType = desfireAppKey.getType();
		KeyType appDefaultKeyType;
		if(desfireApp.getNok().substring(0, 1).equals("8")){
			appDefaultKeyType = KeyType.AES;
			defaultKey = desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000");
		}else{
			appDefaultKeyType = KeyType.DES;
			defaultKey = desFireEV1Service.hexStringToByteArray("0000000000000000");
		}
		
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
					log.debug("Update " + cardId + " - Select root");
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					desfireFlowStep.action = Action.GET_APPS;
					break;
				case GET_APPS:
					log.debug("Update " + cardId + " - Get Apps");
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
 					if(authResultBean.getFullApdu() == null) {
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.getApplicationsIds1());
						authResultBean.setSize(16);
						desfireFlowStep.action = Action.CHECK_APP;
 					}
					break;
				case CHECK_APP: 
					log.debug("Update " + cardId + " - Check Apps");
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(desFireEV1Service.hexStringToByteArray("000000")));
					authResultBean.setSize(16);
					String appsString = result.substring(0, result.length()-4);
					// split result : app id on 6 chars (3 bytes) 
					List<String> apps = Arrays.asList(appsString.split("(?<=\\G.{6})"));
					
					TagUpdateApi appTagUpdateApi = desfireApp.getTagUpdateApi();
					Date lastUpdate = appTagUpdateApi.getLastUpdateDateFromCsn(cardId);
					if(desfireFlowStep.currentApp < desfireTag.getApplications().size()) {
						log.trace("schemaDate : " + schemaDate);
						log.trace("lastUpdate : " + lastUpdate);
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
					log.debug("Update " + cardId + " - Delete App");
					authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.deleteApplication(desFireEV1Service.swapPairsByte(aid)));
						authResultBean.setSize(0);						
						desfireFlowStep.action = Action.CREATE_APP;
					}
					break;
				case CREATE_APP: 
					log.debug("Update " + cardId + " - Create app : " + desFireEV1Service.byteArrayToHexString(aid));
					authResultBean.setFullApdu(desFireEV1Service.createApplication(desFireEV1Service.swapPairsByte(aid),amks, nok));
					authResultBean.setSize(0);
					desfireFlowStep.action = Action.CHANGE_APP_MASTER_KEY;
					break;
				case CHANGE_APP_MASTER_KEY:
					log.debug("Update " + cardId + " - Change Master key app : " + desFireEV1Service.byteArrayToHexString(aid));
					authResultBean = this.authApp(aid, result, defaultKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, (byte) 0x00, DESFireEV1Service.KeyType.AES, appKey, defaultKey));
						authResultBean.setSize(16);					
						log.trace("change key part");
						desfireFlowStep.action = Action.CHANGE_KEYS;
						desfireFlowStep.authStep = 1;
					}
					break;
				case CHANGE_KEYS:	
					log.debug("Update " + cardId + " - Change key app : " + keyNo + " for " + desFireEV1Service.byteArrayToHexString(aid));
					authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, DESFireEV1Service.KeyType.AES, key, defaultKey));
						authResultBean.setSize(16);					
						log.trace("change key part");
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
					if(authResultBean.getFullApdu() == null) {
						log.debug("Update " + cardId + " - Create file in " + desFireEV1Service.byteArrayToHexString(aid) +" : " + fileNo);
						desfireFlowStep.authStep = 1;
						authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(writepayload));
						authResultBean.setSize(16);					
						desfireFlowStep.action = Action.WRITE_FILE;
						desfireFlowStep.authStep = 1;
					} 
					desfireFlowStep.writeStep = 0;
					break;
				case WRITE_FILE:	
					if(authResultBean.getFullApdu() == null || desfireFlowStep.writeStep>0) {
						if(desfireFlowStep.writeStep==0 || result.endsWith("AF")) {
							desfireFlowStep.authStep = 1;
							TagUpdateApi fileTagUpdateApi = desfireFile.getTagUpdateApi();
							String desfireId = fileTagUpdateApi.getIdFromCsn(cardId);
							log.debug("Update " + cardId + " - Write in file " + fileNo +" : " + desfireId);
							authResultBean.setFullApdu(desFireEV1Service.writeData(fileNo, desfireId));
							authResultBean.setSize(16);						
							log.trace("write part");
							desfireFlowStep.writeStep++;
						} else {
							desfireFlowStep.writeStep = 0;
							desfireFlowStep.action = Action.CHANGE_FILE_KEY_SET;
							authResultBean = updateCard(result, cardId);
						} 
					} 
					break;
				case CHANGE_FILE_KEY_SET:	
					log.debug("Update " + cardId + " - Change key settings " + fileNo +" : " + cms + ", " + accessRights[1] +", "+accessRights[0]);
					authResultBean.setFullApdu(desFireEV1Service.changeFileSettings(fileNo, cms, accessRights[1], accessRights[0]));
					authResultBean.setSize(16);					
					log.trace("change key set part");
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
				case CHANGE_PICC_KEY:
					log.debug("change " + cardId + " - PICC master key");
					authResultBean = this.authApp(desFireEV1Service.hexStringToByteArray("000000"), result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
					if(authResultBean.getFullApdu() == null) {
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, piccKeyVerFinish, piccKeyTypeFinish, piccKeyFinish, piccKeyStart));
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
	
	
	public String decriptDesfireId(String apdu){
		byte[] resultByte = desFireEV1Service.hexStringToByteArray(apdu);
		int length = Integer.parseInt(desFireEV1Service.swapPairs(desFireEV1Service.hexStringToByteArray(tempFileSize)), 16);
		byte[] resultDecript = desFireEV1Service.postprocess(resultByte, length, DESFireEV1Service.CommunicationSetting.ENCIPHERED);
		String hexaResult =  desFireEV1Service.byteArrayToHexString(resultDecript);
		log.info("En hexa : " + hexaResult);
		log.info("En hexa : " + hexaResult.substring(0, length));
		return hexaResult;
	}
	

	public String asBytesString(String desfireId) {		
		int size = (int)(desfireId.length()/16)+1;
		desfireId = StringUtils.leftPad(desfireId, size*16, '0');	
		String desfireIdAsBytesString = desFireEV1Service.byteArrayToHexString(desfireId.getBytes());
		log.trace("desfireIdAsBytesString : " + desfireIdAsBytesString);
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
						log.trace("change key part");						
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
						log.trace("change key part");						
						authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, (byte) 0x01, DESFireEV1Service.KeyType.AES, desFireEV1Service.hexStringToByteArray("00000000000000000000000000000000"), desFireEV1Service.hexStringToByteArray("0000000000000000")));
						log.trace(authResultBean.getFullApdu());
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
	
	   public static String leftPad(String originalString, int length,
		         char padCharacter) {
		      String paddedString = originalString;
		      while (paddedString.length() < length) {
		         paddedString = padCharacter + paddedString;
		      }
		      return paddedString;
		   }
	   
}