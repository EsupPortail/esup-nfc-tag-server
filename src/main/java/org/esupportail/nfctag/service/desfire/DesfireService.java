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

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.beans.*;
import org.esupportail.nfctag.beans.DesfireFlowStep.Action;
import org.esupportail.nfctag.beans.NfcResultBean.CODE;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.esupportail.nfctag.service.api.impl.DesfireDamUpdateConfig;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.api.impl.DesfireUpdateConfig;
import org.esupportail.nfctag.service.api.impl.DesfireWriteConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.KeyType;
import org.esupportail.nfctag.web.wsrest.json.JsonFormCryptogram;
import org.esupportail.nfctag.web.wsrest.json.JsonResponseCryptogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DesfireService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DesfireFlowStep desfireFlowStep = new DesfireFlowStep();

	private String desfireId = "";

	private DesfireAuthSession desfireAuthSession;

	public DESFireEV1Service desFireEV1Service;

	private ApplicationsService applicationsService;

	private NfcAuthConfigService nfcAuthConfigService;

	private DesfireDiversificationDamService desfireDiversificationDamService;

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

	public void setDesfireDiversificationDamService(DesfireDiversificationDamService desfireDiversificationDamService) {
		this.desfireDiversificationDamService = desfireDiversificationDamService;
	}

	public Action getStep() {
		return desfireFlowStep.action;
	}

	public int getAuthStep() {
		return desfireFlowStep.authStep;
	}

public NfcResultBean readUid(String result){

		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.READ;
		}

		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);

		switch(desfireFlowStep.action){
			case READ:
				tempRead = "";
				desfireFlowStep.authStep = 1;
				authResultBean.setFullApdu(desFireEV1Service.getVersion1());
				desfireFlowStep.action = Action.MORE;
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
			case END:
				authResultBean.setAction(NfcResultBean.Action.read);
				authResultBean.setFullApdu("END");
				break;
			default:
				break;
		}
		return authResultBean;
	}

	public NfcResultBean readUidWithAuth(String result, DesfireReadConfig desfireReadConfig){

		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.SELECT_ROOT;
		}

		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		byte[] aid = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireAppId());
		byte[] key = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireKey());
		byte keyNo = DesfireUtils.hexStringToByte(desfireReadConfig.getDesfireKeyNumber());

		switch(desfireFlowStep.action){
			case SELECT_ROOT:
				log.debug("ReadUid - Step : auth on app / key");
				authResultBean = this.authApp(aid, result, key, keyNo, KeyType.AES);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					log.debug("ReadUid - Step : getCardUID");
					authResultBean.setFullApdu(desFireEV1Service.getCardUID());
					desfireFlowStep.action = Action.END;
				}
				break;
			case END:
				authResultBean.setAction(NfcResultBean.Action.read);
				authResultBean.setFullApdu("END");
				break;
			default:
				break;
		}
		return authResultBean;
	}


	public NfcResultBean callApdu(String result, String apdu){

		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.READ;
		}

		NfcResultBean resultBean = new NfcResultBean();
		resultBean.setCode(CODE.OK);

		switch(desfireFlowStep.action){
			case READ:
				resultBean.setFullApdu(apdu);
				desfireFlowStep.action = Action.END;
				break;
			case END:
				resultBean.setFullApdu("END");
				desfireFlowStep.action = Action.END;
				break;
			default:
				break;
		}
		return resultBean;
	}

	public NfcResultBean readDesfireId(DesfireReadConfig desfireReadConfig, String result){
		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.GET_FILE_SETTINGS;
		}
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		byte[] aid = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireAppId());
		byte keyNo = DesfireUtils.hexStringToByte(desfireReadConfig.getDesfireKeyNumber());
		byte[] key = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireKey());
		byte fileNo = DesfireUtils.hexStringToByte(desfireReadConfig.getDesfireFileNumber());
		byte[] offset = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireFileOffset());

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
				byte[] length = DesfireUtils.hexStringToByteArray(tempFileSize);
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
			default:
				break;
		}
		return authResultBean;
	}

	public NfcResultBean readDesfireIdNoAuth(DesfireReadConfig desfireReadConfig, String result){
		if(result.length()==0){
			reset();
			desfireFlowStep.action = Action.GET_FILE_SETTINGS;
		}
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setCode(CODE.OK);
		byte[] aid = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireAppId());
		byte fileNo = DesfireUtils.hexStringToByte(desfireReadConfig.getDesfireFileNumber());
		byte[] offset = DesfireUtils.hexStringToByteArray(desfireReadConfig.getDesfireFileOffset());

		switch(desfireFlowStep.action){
			case GET_FILE_SETTINGS:
				tempFileSize = "";
				if(result.length()==0){
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(DesfireUtils.swapPairsByte(aid)));
				}
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
				byte[] length = DesfireUtils.hexStringToByteArray(tempFileSize);
				desfireFlowStep.authStep = 1;
				authResultBean.setFullApdu(desFireEV1Service.readData(fileNo, offset, length));
				authResultBean.setSize(32);
				desfireFlowStep.action = Action.MORE;
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
			default:
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

	public DesfireTag getDesfireTagToDamUpdate() {
		DesfireDamUpdateConfig desfireDamUpdateConfig = (DesfireDamUpdateConfig) desfireAuthSession.getDesfireAuthConfig();
		return desfireDamUpdateConfig.getDesfireTag();
	}

	public NfcResultBean writeCard(String result, String eppnInit, String csn) throws EsupNfcTagException {
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.none);
		authResultBean.setCode(CODE.OK);

		DesfireTag desfireTag =  getDesfireTagToWrite();

		if(result.length()==0){
			reset();
			if(desfireTag.getFormatBeforeWrite()){
				desfireFlowStep.action = Action.FORMAT;
			}else{
				desfireFlowStep.action = Action.SELECT_ROOT;
			}
		}

		byte[] piccKeyStart = DesfireUtils.hexStringToByteArray(desfireTag.getKeyStart());

		byte[] defaultKey = null;

		byte[] piccAid = DesfireUtils.hexStringToByteArray("000000");
		KeyType piccKeyTypeStart = desfireTag.getKeyTypeStart();

		byte[] piccKeyFinish = null;
		KeyType piccKeyTypeFinish = null;
		byte piccKeyVerFinish = 0;

		if(desfireTag.getKeyFinish() != null){
			piccKeyFinish = DesfireUtils.hexStringToByteArray(desfireTag.getKeyFinish());
			piccKeyTypeFinish = desfireTag.getKeyTypeFinish();
			piccKeyVerFinish = DesfireUtils.hexStringToByte(desfireTag.getKeyVersionFinish());
		}

		DesfireApplication desfireApp = null;
		byte[] aid = null;
		byte amks = 0;
		byte nok = 0;

		byte[] appKey = null;
		byte appKeyNo = 0;
		byte appKeyVer = 0;
		KeyType appKeyType = null;
		KeyType appDefaultKeyType = null;

		byte keyNo = 0;
		byte[] key = null;
		byte keyVer = 0;
		KeyType keyKeyType = null;

		byte[] byteWritePayload = null;
		byte[] accessRights = null;
		byte cms = 0;
		byte fileNo = 0;

		if(desfireTag.getApplications() != null  && desfireTag.getApplications().size() >  desfireFlowStep.currentApp) {

			desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
			aid = DesfireUtils.hexStringToByteArray(desfireApp.getDesfireAppId());
			amks= DesfireUtils.hexStringToByte(desfireApp.getAmks());
			nok = DesfireUtils.hexStringToByte(desfireApp.getNok());
				
				/* nok build :
				b1 : 10 for AES 
				b2:  10 for ISO ID, 00 else
				b3-b4 : number of keys
				*/
			if(desfireApp.getNok().substring(0, 1).equals("8") || desfireApp.getNok().substring(0, 1).equals("A")){
				defaultKey = DesfireUtils.hexStringToByteArray("00000000000000000000000000000000");
				appDefaultKeyType = KeyType.AES;
				appKey = DesfireUtils.hexStringToByteArray("00000000000000000000000000000000");
				appKeyNo = DesfireUtils.hexStringToByte("00");
				appKeyVer = DesfireUtils.hexStringToByte("00");
				keyKeyType = appKeyType = KeyType.AES;
			}else{
				defaultKey = DesfireUtils.hexStringToByteArray("0000000000000000");
				appDefaultKeyType = KeyType.DES;
				appKey = DesfireUtils.hexStringToByteArray("0000000000000000");
				appKeyNo = DesfireUtils.hexStringToByte("00");
				appKeyVer = DesfireUtils.hexStringToByte("00");
				keyKeyType = appKeyType = KeyType.DES;
			}

			if(desfireApp.getKeys() != null && desfireApp.getKeys().size() > 0) {
				DesfireKey desfireAppKey = desfireApp.getKeys().get(0);
				appKey = DesfireUtils.hexStringToByteArray(desfireAppKey.getKey());
				appKeyNo = DesfireUtils.hexStringToByte(desfireAppKey.getKeyNo());
				appKeyVer = DesfireUtils.hexStringToByte(desfireAppKey.getKeyVer());
				if(desfireApp.getKeys().size() >  desfireFlowStep.currentKey) {
					DesfireKey desfireKey = desfireApp.getKeys().get(desfireFlowStep.currentKey);
					keyNo = DesfireUtils.hexStringToByte(desfireKey.getKeyNo());
					DesfireKeyService desfireKeyService = desfireKey.getDesfireKeyService();
					String desfireKeyAsHexa = desfireKey.getKey();
					if(desfireKeyService!=null) {
						desfireKeyAsHexa = desfireKeyService.getKeyFromCsn(csn);
					}
					key = DesfireUtils.hexStringToByteArray(desfireKeyAsHexa);
					keyVer = DesfireUtils.hexStringToByte(desfireKey.getKeyVer());
				}
			}

			if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
				DesfireFile desfireFile = desfireApp.getFiles().get(desfireFlowStep.currentFile);
				accessRights = DesfireUtils.hexStringToByteArray(desfireFile.getAccessRights());
				cms = DesfireUtils.hexStringToByte(desfireFile.getCommunicationSettings());
				fileNo = DesfireUtils.hexStringToByte(desfireFile.getFileNumber());
				TagWriteApi tagWriteApi = desfireFile.getTagWriteApi();
				desfireId = tagWriteApi.getIdFromCsn(csn);
				String writePayload;
				if(desfireFile.getFileSize() != null){
					writePayload = desfireFile.getWriteFilePayload((nok|0x20)==nok) + desfireFile.getFileSize();
				} else{
					//calcul automatique de la taille du fichier en fonction du contenu
					int fileSize = desfireId.length() / 2;
					String hexSize = leftPad(Integer.toHexString(fileSize), 6, '0');
					writePayload = desfireFile.getWriteFilePayload((nok|0x20)==nok) + DesfireUtils.swapPairs(DesfireUtils.hexStringToByteArray(hexSize));
				}
				byteWritePayload = DesfireUtils.hexStringToByteArray(writePayload);
			}
		} else {
			// si pas d'application ni master key à changer on valide l'encodage pour affectation de la carte
			if((desfireTag.getApplications() == null || desfireTag.getApplications().size() == 0) && desfireTag.getKeyFinish() == null) {
				log.debug("No applications and no keys to write - goal is just to read uid and assign the tag");
				desfireFlowStep.action = Action.END;
			}
		}

		switch(desfireFlowStep.action){
			case FORMAT:
				log.debug("Write by " + eppnInit + " - Step : Format card");
				authResultBean = this.authApp(piccAid, result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					if (desfireTag.getDamKeysTagWriteApi()!=null && desfireFlowStep.resetStep == 0) {
						log.debug("Reset DamAuthKey - Step : Format card");
						try {
							authResultBean.setFullApdu(desFireEV1Service.changeDamKey((byte) 0x10, (byte) 0x00, KeyType.AES, new byte[16], desfireDiversificationDamService.getDamAuthKey(desfireTag.getDamKeysTagWriteApi(), csn)));
						} catch (Exception e) {
							throw new EsupNfcTagException("Reset DamAuthKey failed", e);
						}
						authResultBean.setSize(16);
						desfireFlowStep.resetStep++;
					} else if (desfireTag.getDamKeysTagWriteApi()!=null && desfireFlowStep.resetStep == 1) {
						log.debug("Reset DamEncKey - Step : Format card");
						try {
							authResultBean.setFullApdu(desFireEV1Service.changeDamKey((byte) 0x12, (byte) 0x00, KeyType.AES, new byte[16], desfireDiversificationDamService.getDamEncKey(desfireTag.getDamKeysTagWriteApi(), csn)));
						} catch (Exception e) {
							throw new EsupNfcTagException("Reset DamEncKey failed", e);
						}
						authResultBean.setSize(16);
						desfireFlowStep.resetStep++;
					} else if (desfireTag.getDamKeysTagWriteApi()!=null && desfireFlowStep.resetStep == 2) {
						log.debug("Reset DamMacKey - Step : Format card");
						try {
							authResultBean.setFullApdu(desFireEV1Service.changeDamKey((byte) 0x11, (byte) 0x00, KeyType.AES, new byte[16], desfireDiversificationDamService.getDamMacKey(desfireTag.getDamKeysTagWriteApi(), csn)));
							desfireDiversificationDamService.resetDamBaseKey(desfireTag.getDamKeysTagWriteApi(), csn);
						} catch (Exception e) {
							throw new EsupNfcTagException("Reset DamMacKey failed", e);
						}
						authResultBean.setSize(16);
						desfireFlowStep.resetStep++;
					} else {
						authResultBean.setFullApdu(desFireEV1Service.formatPICC());
						authResultBean.setSize(16);
						if (desfireTag.getApplications() != null && desfireTag.getApplications().size() > desfireFlowStep.currentApp) {
							if ("000000".equals(desfireApp.getDesfireAppId()) && desfireApp.getKeys().size() > 0) {
								desfireFlowStep.currentKey = 1;
								desfireFlowStep.action = Action.CHANGE_KEYS;
							} else {
								desfireFlowStep.action = Action.CREATE_APP;
							}
						} else {
							if (piccKeyFinish != null) {
								desfireFlowStep.currentApp = 0;
								desfireFlowStep.action = Action.CHANGE_PICC_KEY;
							} else if (desfireTag.getDamKeysTagWriteApi()!=null) {
								desfireFlowStep.currentApp = 0;
								desfireFlowStep.action = Action.LOAD_DAM_KEYS;
							}
						}
					}
				}
				break;
			case SELECT_ROOT:
				log.debug("Write by " + eppnInit + " - Step : Select root");
				authResultBean = this.authApp(piccAid, result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.selectApplication(piccAid));
					authResultBean.setSize(16);
					if(desfireTag.getApplications() != null &&  desfireTag.getApplications().size() > desfireFlowStep.currentApp){
						if("000000".equals(desfireApp.getDesfireAppId()) && desfireApp.getKeys().size()>0) {
							desfireFlowStep.currentKey = 1;
							desfireFlowStep.action = Action.CHANGE_KEYS;
						} else {
							desfireFlowStep.action = Action.CREATE_APP;
						}
					} else if(piccKeyFinish != null){
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.CHANGE_PICC_KEY;
					} else if(desfireTag.getDamKeysTagWriteApi()!=null){
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.LOAD_DAM_KEYS;
					} else {
						desfireFlowStep.action = Action.END;
					}
				}
				break;
			case CREATE_APP:
				log.debug("Write by " + eppnInit + " - Step : Create app : " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(piccAid, result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					if((nok|0x20)==nok) {
						log.debug("Iso Application requested via nok");
						assert(desfireApp.getIsoId()!= null && desfireApp.getIsoName() != null);
					} else {
						log.debug("Iso Application NOT requested via nok");
						assert(desfireApp.getIsoId() == null && desfireApp.getIsoName() == null);
					}
					authResultBean.setFullApdu(desFireEV1Service.createApplication(DesfireUtils.swapPairsByte(aid),amks, nok,(nok|0x20)==nok, desfireApp.getLsbIsoId(), desfireApp.getIsoName()));
					authResultBean.setSize(0);
					if(appKey != null){
						desfireFlowStep.action = Action.CHANGE_APP_MASTER_KEY;
					}else{
						desfireFlowStep.action = Action.CREATE_FILE;
						desfireFlowStep.currentKey = 1;
					}
				}
				break;
			case CHANGE_APP_MASTER_KEY:
				log.debug("Write by " + eppnInit + " - Step : Change app key : " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(aid, result, defaultKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey(appKeyNo, appKeyVer, appKeyType, appKey, defaultKey));
					authResultBean.setSize(16);
					if(desfireApp.getKeys() != null && desfireApp.getKeys().size() >  1) {
						desfireFlowStep.currentKey = 1;
						desfireFlowStep.action = Action.CHANGE_KEYS;
					} else if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
						desfireFlowStep.action = Action.CREATE_FILE;
						desfireFlowStep.currentKey = 1;
					} else {
						desfireFlowStep.currentFile = 0;
						desfireFlowStep.currentApp++;
						if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
							desfireFlowStep.action = Action.CREATE_APP;
						} else if(piccKeyFinish != null){
							desfireFlowStep.currentApp = 0;
							desfireFlowStep.action = Action.CHANGE_PICC_KEY;
						} else if(desfireTag.getDamKeysTagWriteApi()!=null){
							desfireFlowStep.currentApp = 0;
							desfireFlowStep.action = Action.LOAD_DAM_KEYS;
						} else {
							desfireFlowStep.action = Action.END;
						}
					}
				}
				break;
			case CHANGE_KEYS:
				log.debug("Write by " + eppnInit + " - Step : Change app key : " + keyNo + " for " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(aid, result, appKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, keyKeyType, key, defaultKey));
					authResultBean.setSize(16);
					log.trace("change key part");
					if(desfireApp.getKeys().size() >  desfireFlowStep.currentKey + 1){
						desfireFlowStep.currentKey++;
						desfireFlowStep.action = Action.CHANGE_KEYS;
					} else {
						if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
							desfireFlowStep.action = Action.CREATE_FILE;
							desfireFlowStep.currentKey = 1;
						} else {
							desfireFlowStep.currentFile = 0;
							desfireFlowStep.currentApp++;
							if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
								desfireFlowStep.action = Action.CREATE_APP;
							} else if(piccKeyFinish != null){
								desfireFlowStep.currentApp = 0;
								desfireFlowStep.action = Action.CHANGE_PICC_KEY;
							} else if(desfireTag.getDamKeysTagWriteApi()!=null){
								desfireFlowStep.currentApp = 0;
								desfireFlowStep.action = Action.LOAD_DAM_KEYS;
							}else {
								desfireFlowStep.action = Action.END;
							}
						}
					}
				}
				break;
			case CREATE_FILE:
				log.debug("Write by " + eppnInit + " - Step : Create file in " + DesfireUtils.byteArrayToHexString(aid) +" : " + fileNo);
				authResultBean = this.authApp(aid, result, appKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(byteWritePayload, (nok|0x20)==nok));
					authResultBean.setSize(16);
					desfireFlowStep.writeStep = 0;
					desfireFlowStep.action = Action.WRITE_FILE;
				}
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
				log.debug("Write by " + eppnInit + " - Step : Change key settings " + fileNo +" : " + cms + ", " + accessRights[1] +", "+accessRights[0]);
				authResultBean.setFullApdu(desFireEV1Service.changeFileSettings(fileNo, cms, accessRights[1], accessRights[0]));
				authResultBean.setSize(16);
				if(desfireApp.getFiles().size() >  desfireFlowStep.currentFile + 1){
					desfireFlowStep.currentFile++;
					desfireFlowStep.action = Action.CREATE_FILE;
				}else{
					desfireFlowStep.currentFile = 0;
					desfireFlowStep.currentApp++;
					if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
						desfireFlowStep.action = Action.CREATE_APP;
					} else if(piccKeyFinish != null){
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.CHANGE_PICC_KEY;
					} else if(desfireTag.getDamKeysTagWriteApi()!=null){
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.LOAD_DAM_KEYS;
					} else {
						desfireFlowStep.action = Action.END;
					}
				}
				break;
			case CHANGE_PICC_KEY:
				log.debug("Write by " + eppnInit + " - Step : PICC master key");
				authResultBean = this.authApp(piccAid, result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey((byte) 0x00, piccKeyVerFinish, piccKeyTypeFinish, piccKeyFinish, piccKeyStart));
					authResultBean.setSize(16);
					if(desfireTag.getDamKeysTagWriteApi()!=null){
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.LOAD_DAM_KEYS;
					} else {
						desfireFlowStep.action = Action.END;
					}
				}
				break;
			case LOAD_DAM_KEYS:
				log.debug("Write by " + eppnInit + " - Step : Load dam keys");

				authResultBean = this.authApp(piccAid, result, piccKeyFinish, (byte) 0x00, piccKeyTypeFinish);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					if (desfireFlowStep.damKeysStep == 0) {
						log.debug("Create DamBaseKey - Step : Load dam keys");
						desfireDiversificationDamService.createDamBaseKey(desfireTag.getDamKeysTagWriteApi(), csn);
						log.debug("Load DamAuthKey - Step : Load dam keys");
						try {
							authResultBean.setFullApdu(desFireEV1Service.changeDamKey((byte) 0x10, (byte) 0x00, KeyType.AES, desfireDiversificationDamService.getDamAuthKey(desfireTag.getDamKeysTagWriteApi(), csn), new byte[16]));
						} catch (Exception e) {
							throw new EsupNfcTagException("Load DamAuthKey failed", e);
						}
						authResultBean.setSize(16);
						desfireFlowStep.damKeysStep++;
					} else if (desfireFlowStep.damKeysStep == 1) {
						log.debug("Load DamEncKey - Step : Load dam keys");
						try {
							authResultBean.setFullApdu(desFireEV1Service.changeDamKey((byte) 0x12, (byte) 0x00, KeyType.AES, desfireDiversificationDamService.getDamEncKey(desfireTag.getDamKeysTagWriteApi(), csn), new byte[16]));
						} catch (Exception e) {
							throw new EsupNfcTagException("Load DamEncKey failed", e);
						}
						authResultBean.setSize(16);
						desfireFlowStep.damKeysStep++;
					} else if (desfireFlowStep.damKeysStep == 2) {
						log.debug("Load DamMacKey - Step : Load dam keys");
						try {
							authResultBean.setFullApdu(desFireEV1Service.changeDamKey((byte) 0x11, (byte) 0x00, KeyType.AES, desfireDiversificationDamService.getDamMacKey(desfireTag.getDamKeysTagWriteApi(), csn), new byte[16]));
						} catch (Exception e) {
							throw new EsupNfcTagException("Load DamMacKey failed", e);
						}
						authResultBean.setSize(16);
						desfireFlowStep.action = Action.END;
					}
				}
				break;
			case END:
				authResultBean.setAction(NfcResultBean.Action.write);
				authResultBean.setFullApdu("END");
				break;
			default:
				break;
		}
		return authResultBean;
	}

	public NfcResultBean updateCard(String result, String eppnInit, String csn) throws EsupNfcTagException {
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.none);
		authResultBean.setCode(CODE.OK);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date schemaDate = null;

		byte[] defaultKey = null;
		DesfireTag desfireTag =  getDesfireTagToUpdate();

		if(result.length()==0){
			reset();
			if(desfireTag.getFormatBeforeWrite()){
				desfireFlowStep.action = Action.FORMAT;
			}else{
				desfireFlowStep.action = Action.GET_APPS;
			}
		}
		byte[] piccAid = DesfireUtils.hexStringToByteArray("000000");
		byte[] piccKeyStart = DesfireUtils.hexStringToByteArray(desfireTag.getKeyStart());
		KeyType piccKeyTypeStart = desfireTag.getKeyTypeStart();

		byte[] piccKeyFinish = null;
		KeyType piccKeyTypeFinish = null;
		byte piccKeyVerFinish = 0;

		if(desfireTag.getKeyFinish() != null){
			piccKeyFinish = DesfireUtils.hexStringToByteArray(desfireTag.getKeyFinish());
			piccKeyTypeFinish = desfireTag.getKeyTypeFinish();
			piccKeyVerFinish = DesfireUtils.hexStringToByte(desfireTag.getKeyVersionFinish());
		}

		DesfireApplication desfireApp = null;
		byte[] aid = null;
		byte amks = 0;
		byte nok = 0;

		byte[] appKey = null;
		byte appKeyNo = 0;
		byte appKeyVer = 0;
		KeyType appKeyType = null;
		KeyType appDefaultKeyType = null;

		byte keyNo = 0;
		byte[] key = null;
		byte keyVer = 0;
		KeyType keyKeyType = null;

		byte[] byteWritePayload = null;
		byte[] accessRights = null;
		byte cms = 0;
		byte fileNo = 0;

		DesfireFile desfireFile = null;

		String writePayload = null;

		if(desfireTag.getApplications() != null && desfireTag.getApplications().size() >  desfireFlowStep.currentApp){
			desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
			aid = DesfireUtils.hexStringToByteArray(desfireApp.getDesfireAppId());
			amks= DesfireUtils.hexStringToByte(desfireApp.getAmks());
			nok = DesfireUtils.hexStringToByte(desfireApp.getNok());

			if(desfireApp.getNok().substring(0, 1).equals("8") || desfireApp.getNok().substring(0, 1).equals("A")){
				defaultKey = DesfireUtils.hexStringToByteArray("00000000000000000000000000000000");
				appDefaultKeyType = KeyType.AES;
				appKey = DesfireUtils.hexStringToByteArray("00000000000000000000000000000000");
				appKeyNo = DesfireUtils.hexStringToByte("00");
				appKeyVer = DesfireUtils.hexStringToByte("00");
				keyKeyType = appKeyType = KeyType.AES;
			}else{
				defaultKey = DesfireUtils.hexStringToByteArray("0000000000000000");
				appDefaultKeyType = KeyType.DES;
				appKey = DesfireUtils.hexStringToByteArray("0000000000000000");
				appKeyNo = DesfireUtils.hexStringToByte("00");
				appKeyVer = DesfireUtils.hexStringToByte("00");
				keyKeyType = appKeyType = KeyType.DES;
			}

			try {
				schemaDate = formatter.parse(desfireApp.getUpdateDate());
			} catch (ParseException e) {
				log.error(e.getMessage());
			}

			if(desfireApp.getKeys() != null && desfireApp.getKeys().size() > 0) {
				DesfireKey desfireAppKey = desfireApp.getKeys().get(0);
				appKey = DesfireUtils.hexStringToByteArray(desfireAppKey.getKey());
				appKeyNo = DesfireUtils.hexStringToByte(desfireAppKey.getKeyNo());
				appKeyVer = DesfireUtils.hexStringToByte(desfireAppKey.getKeyVer());
				if(desfireApp.getKeys().size() >  desfireFlowStep.currentKey) {
					DesfireKey desfireKey = desfireApp.getKeys().get(desfireFlowStep.currentKey);
					keyNo = DesfireUtils.hexStringToByte(desfireKey.getKeyNo());
					DesfireKeyService desfireKeyService = desfireKey.getDesfireKeyService();
					String desfireKeyAsHexa = desfireKey.getKey();
					if(desfireKeyService!=null) {
						desfireKeyAsHexa = desfireKeyService.getKeyFromCsn(csn);
					}
					key = DesfireUtils.hexStringToByteArray(desfireKeyAsHexa);
					keyVer = DesfireUtils.hexStringToByte(desfireKey.getKeyVer());
				}
			}

			if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
				desfireFile = desfireApp.getFiles().get(desfireFlowStep.currentFile);
				accessRights = DesfireUtils.hexStringToByteArray(desfireFile.getAccessRights());
				cms = DesfireUtils.hexStringToByte(desfireFile.getCommunicationSettings());
				fileNo = DesfireUtils.hexStringToByte(desfireFile.getFileNumber());
				TagWriteApi tagWriteApi = desfireFile.getTagWriteApi();
				desfireId = tagWriteApi.getIdFromCsn(csn);

				if(desfireFile.getFileSize() != null){
					writePayload = desfireFile.getWriteFilePayload((nok|0x20)==nok) + desfireFile.getFileSize();
				} else{
					//calcul automatique de la taille du fichier en fonction du contenu
					int fileSize = desfireId.length() / 2;
					String hexSize = leftPad(Integer.toHexString(fileSize), 6, '0');
					writePayload = desfireFile.getWriteFilePayload((nok|0x20)==nok) + DesfireUtils.swapPairs(DesfireUtils.hexStringToByteArray(hexSize));
				}
				byteWritePayload = DesfireUtils.hexStringToByteArray(writePayload);
			}
		}


		switch(desfireFlowStep.action){
			case FORMAT:
				log.debug("Write by " + eppnInit + " - Step : Format card");
				authResultBean = this.authApp(piccAid, result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.formatPICC());
					authResultBean.setSize(16);
					if(desfireTag.getApplications() != null &&  desfireTag.getApplications().size() > desfireFlowStep.currentApp){
						desfireFlowStep.action = Action.CREATE_APP;
					} else {
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.CHANGE_PICC_KEY;
					}
				}
				break;
			case GET_APPS:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Get Apps");
				authResultBean = this.authApp(piccAid, result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.getApplicationsIds1());
					authResultBean.setSize(16);
					desfireFlowStep.action = Action.CHECK_APP;
				}
				break;
			case CHECK_APP:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Check Apps");
				authResultBean.setFullApdu(desFireEV1Service.selectApplication(DesfireUtils.hexStringToByteArray("000000")));
				authResultBean.setSize(16);
				String appsString = result.substring(0, result.length()-4);
				// split result : app id on 6 chars (3 bytes)
				List<String> apps = Arrays.asList(appsString.split("(?<=\\G.{6})"));
				Date lastUpdate = desfireApp.getTagLastUpdateRestWs().getLastUpdateDateFromCsn(csn);
				if(desfireFlowStep.currentApp < desfireTag.getApplications().size()) {
					log.debug("Check app dates : schemaDate = " + schemaDate + ", lastUpdate : " + lastUpdate);
					if(schemaDate!=null && lastUpdate.before(schemaDate)){
						authResultBean.setAction(NfcResultBean.Action.update);
						if(!apps.contains(DesfireUtils.byteArrayToHexString(DesfireUtils.swapPairsByte(aid)))) {
							desfireFlowStep.action = Action.CREATE_APP;
						}else{
							desfireFlowStep.action = Action.DELETE_APP;
						}
					}else{
						if(desfireFlowStep.currentApp + 1 < desfireTag.getApplications().size()) {
							desfireFlowStep.currentApp++;
							desfireFlowStep.action = Action.GET_APPS;
						}else{
							desfireFlowStep.currentApp = 0;
							desfireFlowStep.action = Action.END;
						}
					}
				}
				break;
			case DELETE_APP:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Delete App");
				authResultBean = this.authApp(aid, result, appKey, (byte) 0x00, KeyType.AES);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.deleteApplication(DesfireUtils.swapPairsByte(aid)));
					authResultBean.setSize(0);
					desfireFlowStep.action = Action.CREATE_APP;
				}
				break;
			case CREATE_APP:
				log.debug("Update by " + eppnInit + "with csn : " + csn + " - Step : Create app : " + DesfireUtils.byteArrayToHexString(aid));
				if((nok|0x20)==nok) {
					log.debug("Iso Application requested via nok");
					assert(desfireApp.getIsoId()!= null && desfireApp.getIsoName() != null);
				} else {
					log.debug("Iso Application NOT requested via nok");
					assert(desfireApp.getIsoId() == null && desfireApp.getIsoName() == null);
				}
				authResultBean.setFullApdu(desFireEV1Service.createApplication(DesfireUtils.swapPairsByte(aid),amks, nok,(nok|0x20)==nok, desfireApp.getLsbIsoId(), desfireApp.getIsoName()));
				authResultBean.setSize(0);
				desfireFlowStep.action = Action.CHANGE_APP_MASTER_KEY;
				break;
			case CHANGE_APP_MASTER_KEY:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Change app key : " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(aid, result, defaultKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey(appKeyNo, appKeyVer, appKeyType, appKey, defaultKey));
					authResultBean.setSize(16);
					if(desfireApp.getKeys() != null && desfireApp.getKeys().size() >  1) {
						desfireFlowStep.currentKey = 1;
						desfireFlowStep.action = Action.CHANGE_KEYS;
					} else if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
						desfireFlowStep.action = Action.CREATE_FILE;
						desfireFlowStep.currentKey = 1;
					} else {
						desfireFlowStep.currentFile = 0;
						desfireFlowStep.currentApp++;
						if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
							desfireFlowStep.action = Action.CREATE_APP;
						} else if(piccKeyFinish != null){
							desfireFlowStep.currentApp = 0;
							desfireFlowStep.action = Action.CHANGE_PICC_KEY;
						} else {
							desfireFlowStep.action = Action.END;
						}
					}
				}
				break;
			case CHANGE_KEYS:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Change app key : " + keyNo + " for " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(aid, result, appKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, keyKeyType, key, defaultKey));
					authResultBean.setSize(16);
					log.trace("change key part");
					if(desfireApp.getKeys().size() >  desfireFlowStep.currentKey + 1){
						desfireFlowStep.currentKey++;
						desfireFlowStep.action = Action.CHANGE_KEYS;
					} else {
						if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
							desfireFlowStep.action = Action.CREATE_FILE;
							desfireFlowStep.currentKey = 1;
						} else {
							desfireFlowStep.currentFile = 0;
							desfireFlowStep.currentApp++;
							if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
								desfireFlowStep.action = Action.CREATE_APP;
							} else if(piccKeyFinish != null){
								desfireFlowStep.currentApp = 0;
								desfireFlowStep.action = Action.CHANGE_PICC_KEY;
							} else {
								desfireFlowStep.action = Action.END;
							}
						}
					}
				}

				break;
			case CREATE_FILE:
				if(authResultBean.getFullApdu() == null) {
					log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Create file in " + DesfireUtils.byteArrayToHexString(aid) +" : " + fileNo);
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(byteWritePayload, (nok|0x20)==nok));
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
						log.debug("Update by " + eppnInit + " - Write in file " + fileNo +" : " + desfireId);
					} else {
						desfireFlowStep.writeStep = 0;
						desfireFlowStep.action = Action.CHANGE_FILE_KEY_SET;
						authResultBean = updateCard(result, eppnInit, csn);
					}
				}
				break;
			case CHANGE_FILE_KEY_SET:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Change key settings " + fileNo +" : " + cms + ", " + accessRights[1] +", "+accessRights[0]);
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
						desfireFlowStep.action = Action.GET_APPS;
					} else {
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.END;
					}
				}
				break;
			case CHANGE_PICC_KEY:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : PICC master key");
				authResultBean = this.authApp(DesfireUtils.hexStringToByteArray("000000"), result, piccKeyStart, (byte) 0x00, piccKeyTypeStart);
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
			default:
				break;
		}
		return authResultBean;
	}

	public NfcResultBean damUpdateCard(String result, String eppnInit, String csn) throws EsupNfcTagException {
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.none);
		authResultBean.setCode(CODE.OK);

		byte[] defaultKey = null;
		DesfireTag desfireTag =  getDesfireTagToDamUpdate();

		if(result.length()==0){
			reset();
			if(desfireTag.getFormatBeforeWrite()){
				desfireFlowStep.action = Action.FORMAT;
			}else{
				desfireFlowStep.action = Action.CREATE_APP;
			}
		}
		byte[] piccAid = DesfireUtils.hexStringToByteArray("000000");


		DesfireApplication desfireApp = null;
		byte[] aid = null;
		byte amks = 0;
		byte nok = 0;

		byte[] appKey = null;
		byte appKeyNo = 0;
		byte appKeyVer = 0;
		KeyType appKeyType = null;
		KeyType appDefaultKeyType = null;

		byte keyNo = 0;
		byte[] key = null;
		byte keyVer = 0;
		KeyType keyKeyType = null;

		byte[] byteWritePayload = null;
		byte[] accessRights = null;
		byte cms = 0;
		byte fileNo = 0;

		DesfireFile desfireFile = null;

		String writePayload = null;

		if(desfireTag.getApplications() != null && desfireTag.getApplications().size() >  desfireFlowStep.currentApp){
			desfireApp = desfireTag.getApplications().get(desfireFlowStep.currentApp);
			aid = DesfireUtils.hexStringToByteArray(desfireApp.getDesfireAppId());
			amks= DesfireUtils.hexStringToByte(desfireApp.getAmks());
			nok = DesfireUtils.hexStringToByte(desfireApp.getNok());

			if(desfireApp.getNok().substring(0, 1).equals("8") || desfireApp.getNok().substring(0, 1).equals("A")){
				defaultKey = DesfireUtils.hexStringToByteArray("00000000000000000000000000000000");
				appDefaultKeyType = KeyType.AES;
				appKey = DesfireUtils.hexStringToByteArray("00000000000000000000000000000000");
				appKeyNo = DesfireUtils.hexStringToByte("00");
				appKeyVer = DesfireUtils.hexStringToByte("00");
				keyKeyType = appKeyType = KeyType.AES;
			}else{
				defaultKey = DesfireUtils.hexStringToByteArray("0000000000000000");
				appDefaultKeyType = KeyType.DES;
				appKey = DesfireUtils.hexStringToByteArray("0000000000000000");
				appKeyNo = DesfireUtils.hexStringToByte("00");
				appKeyVer = DesfireUtils.hexStringToByte("00");
				keyKeyType = appKeyType = KeyType.DES;
			}

			if(desfireApp.getKeys() != null && desfireApp.getKeys().size() > 0) {
				DesfireKey desfireAppKey = desfireApp.getKeys().get(0);
				appKey = DesfireUtils.hexStringToByteArray(desfireAppKey.getKey());
				appKeyNo = DesfireUtils.hexStringToByte(desfireAppKey.getKeyNo());
				appKeyVer = DesfireUtils.hexStringToByte(desfireAppKey.getKeyVer());
				if(desfireApp.getKeys().size() >  desfireFlowStep.currentKey) {
					DesfireKey desfireKey = desfireApp.getKeys().get(desfireFlowStep.currentKey);
					keyNo = DesfireUtils.hexStringToByte(desfireKey.getKeyNo());
					DesfireKeyService desfireKeyService = desfireKey.getDesfireKeyService();
					String desfireKeyAsHexa = desfireKey.getKey();
					if(desfireKeyService!=null) {
						desfireKeyAsHexa = desfireKeyService.getKeyFromCsn(csn);
					}
					key = DesfireUtils.hexStringToByteArray(desfireKeyAsHexa);
					keyVer = DesfireUtils.hexStringToByte(desfireKey.getKeyVer());
				}
			}

			if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
				desfireFile = desfireApp.getFiles().get(desfireFlowStep.currentFile);
				accessRights = DesfireUtils.hexStringToByteArray(desfireFile.getAccessRights());
				cms = DesfireUtils.hexStringToByte(desfireFile.getCommunicationSettings());
				fileNo = DesfireUtils.hexStringToByte(desfireFile.getFileNumber());
				TagWriteApi tagWriteApi = desfireFile.getTagWriteApi();
				desfireId = tagWriteApi.getIdFromCsn(csn);
				if(desfireFile.getFileSize() != null){
					writePayload = desfireFile.getWriteFilePayload((nok|0x20)==nok) + desfireFile.getFileSize();
				} else{
					//calcul automatique de la taille du fichier en fonction du contenu
					int fileSize = desfireId.length() / 2;
					String hexSize = leftPad(Integer.toHexString(fileSize), 6, '0');
					writePayload = desfireFile.getWriteFilePayload((nok|0x20)==nok) + DesfireUtils.swapPairs(DesfireUtils.hexStringToByteArray(hexSize));
				}
				byteWritePayload = DesfireUtils.hexStringToByteArray(writePayload);
			}
		}


		switch(desfireFlowStep.action){
			case CREATE_APP:
				log.debug("Update by " + eppnInit + "with csn : " + csn + " - Step : Create app : " + DesfireUtils.byteArrayToHexString(aid));
				if (desfireFlowStep.createDamStep == 0) {
					try {
						authResultBean = this.authApp(piccAid, result, DesfireUtils.hexStringToByteArray(desfireApp.getDamTagWriteApi().getDamAuthKey(csn).getDamAuthKey()), (byte) 0x10, KeyType.AES);
					} catch (NullPointerException e) {
						throw new EsupNfcTagException("TagWrite not define", e);
					}
				}
				if(authResultBean.getFullApdu() == null) {
					if (desfireFlowStep.createDamStep == 0) {
						if ((nok | 0x20) == nok) {
							log.debug("Iso Application requested via nok");
							assert (desfireApp.getIsoId() != null && desfireApp.getIsoName() != null);
						} else {
							log.debug("Iso Application NOT requested via nok");
							assert (desfireApp.getIsoId() == null && desfireApp.getIsoName() == null);
						}
						JsonFormCryptogram jsonFormCryptogram = new JsonFormCryptogram();
						jsonFormCryptogram.setCsn(csn);
						jsonFormCryptogram.setAid(DesfireUtils.byteArrayToHexString(aid));
						jsonFormCryptogram.setDamDefaultKey(DesfireUtils.byteArrayToHexString(new byte[16]));
						jsonFormCryptogram.setDamDefaultKeyVersion("0");
						jsonFormCryptogram.setKeySetting1(desfireApp.getAmks());
						jsonFormCryptogram.setKeySetting2(desfireApp.getNok());
						int sumFileSize = 0;
						for (int i = 0; i < desfireApp.getFiles().size(); i++) {
							sumFileSize += Integer.parseInt(DesfireUtils.swapPairs(DesfireUtils.hexStringToByteArray(desfireApp.getFiles().get(i).getFileSize())), 16);
						}
						int quotaLimit = (Math.floorDiv(sumFileSize, 32) + 1 + desfireApp.getKeys().size()) * 2; // *2 WTF ?
						jsonFormCryptogram.setQuotaLimit(String.valueOf(quotaLimit));
						byte[] isoDfId = null;
						byte[] isoName = null;
						int isoNameLen = 0;
						if (desfireApp.getIsoId() != null && desfireApp.getIsoName() != null) {
							jsonFormCryptogram.setIsoDfName(desfireApp.getIsoName());
							jsonFormCryptogram.setIsoDfId(desfireApp.getIsoId());
							isoDfId = DesfireUtils.hexStringToByteArray(desfireApp.getLsbIsoId());
							isoName = DesfireUtils.hexStringToByteArray(desfireApp.getIsoName());
							isoNameLen = isoName.length;
						}
						log.info(jsonFormCryptogram.toString());
						JsonResponseCryptogram jsonResponseCryptogram = desfireApp.getDamTagWriteApi().getCryptogram(jsonFormCryptogram);
						String apdu = desFireEV1Service.createDelegatedApplication(DesfireUtils.swapPairsByte(aid), jsonResponseCryptogram.getDamSlotNO(), jsonResponseCryptogram.getDamSlotVersion(), quotaLimit, amks, nok, (byte) 0x00, (byte) 0x00,
								(byte) 0x00, (byte) 0x00, (byte) 0x00, isoDfId, isoName, isoNameLen, DesfireUtils.hexStringToByteArray(jsonResponseCryptogram.getEncK()),
								DesfireUtils.hexStringToByteArray(jsonResponseCryptogram.getDammac()));
						authResultBean.setFullApdu(apdu);
						authResultBean.setSize(0);
						desfireFlowStep.createDamStep++;
					} else if (desfireFlowStep.createDamStep == 1) {
						desfireFlowStep.authStep = 1;
						String apdu = desFireEV1Service.createDelegatedApplicationAf();
						authResultBean.setFullApdu(apdu);
						authResultBean.setSize(0);
						desfireFlowStep.createDamStep++;
						desfireFlowStep.action = Action.CHANGE_APP_MASTER_KEY;
					}
				}
				break;
			case CHANGE_APP_MASTER_KEY:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Change app key : " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(aid, result, defaultKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey(appKeyNo, appKeyVer, appKeyType, appKey, defaultKey));
					authResultBean.setSize(16);
					if(desfireApp.getKeys() != null && desfireApp.getKeys().size() >  1) {
						desfireFlowStep.currentKey = 1;
						desfireFlowStep.action = Action.CHANGE_KEYS;
					} else if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
						desfireFlowStep.action = Action.CREATE_FILE;
						desfireFlowStep.currentKey = 1;
					} else {
						desfireFlowStep.currentFile = 0;
						desfireFlowStep.currentApp++;
						if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
							desfireFlowStep.action = Action.CREATE_APP;
						} else {
							desfireFlowStep.action = Action.END;
						}
					}
				}
				break;
			case CHANGE_KEYS:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Change app key : " + keyNo + " for " + DesfireUtils.byteArrayToHexString(aid));
				authResultBean = this.authApp(aid, result, appKey, appKeyNo, appDefaultKeyType);
				if(authResultBean.getFullApdu() == null) {
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.changeKey(keyNo, keyVer, keyKeyType, key, defaultKey));
					authResultBean.setSize(16);
					log.trace("change key part");
					if(desfireApp.getKeys().size() >  desfireFlowStep.currentKey + 1){
						desfireFlowStep.currentKey++;
						desfireFlowStep.action = Action.CHANGE_KEYS;
					} else {
						if(desfireApp.getFiles() != null && desfireApp.getFiles().size() >  desfireFlowStep.currentFile){
							desfireFlowStep.action = Action.CREATE_FILE;
							desfireFlowStep.currentKey = 1;
						} else {
							desfireFlowStep.currentFile = 0;
							desfireFlowStep.currentApp++;
							if(desfireTag.getApplications().size() > desfireFlowStep.currentApp){
								desfireFlowStep.action = Action.CREATE_APP;
							} else {
								desfireFlowStep.action = Action.END;
							}
						}
					}
				}

				break;
			case CREATE_FILE:
				if(authResultBean.getFullApdu() == null) {
					log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Create file in " + DesfireUtils.byteArrayToHexString(aid) +" : " + fileNo);
					desfireFlowStep.authStep = 1;
					authResultBean.setFullApdu(desFireEV1Service.createStdDataFile(byteWritePayload, (nok|0x20)==nok));
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
						log.debug("Update by " + eppnInit + " - Write in file " + fileNo +" : " + desfireId);
					} else {
						desfireFlowStep.writeStep = 0;
						desfireFlowStep.action = Action.CHANGE_FILE_KEY_SET;
						authResultBean = damUpdateCard(result, eppnInit, csn);
					}
				}
				break;
			case CHANGE_FILE_KEY_SET:
				log.debug("Update by " + eppnInit + " with csn : " + csn + " - Step : Change key settings " + fileNo +" : " + cms + ", " + accessRights[1] +", "+accessRights[0]);
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
						desfireFlowStep.action = Action.GET_APPS;
					} else {
						desfireFlowStep.currentApp = 0;
						desfireFlowStep.action = Action.END;
					}
				}
				break;
			case END:
				authResultBean.setFullApdu("END");
				break;
			default:
				break;
		}
		return authResultBean;
	}

	public NfcResultBean authApp4ckeckKey(byte[] aid, String result, byte[] key, byte keyNo, KeyType keyType) {
		desfireFlowStep.action=Action.AUTH;
		return authApp(aid, result, key, keyNo, keyType);
	}

	NfcResultBean authApp(byte[] aid, String result, byte[] key, byte keyNo, KeyType keyType) {
		if(result.length()==0){
			desfireFlowStep.authStep = 1;
		}
		NfcResultBean authResultBean = new NfcResultBean();
		authResultBean.setAction(NfcResultBean.Action.auth);
		authResultBean.setCode(CODE.OK);
		switch(desfireFlowStep.authStep){
			case 1:
				authResultBean.setFullApdu(desFireEV1Service.selectApplication(DesfireUtils.swapPairsByte(aid)));
				authResultBean.setSize(0);
				desfireFlowStep.authStep++;
				break;
			case 2:
				authResultBean.setFullApdu(desFireEV1Service.authenticate1(key,keyNo, keyType));
				authResultBean.setSize(16);
				desfireFlowStep.authStep++;
				break;
			case 3:
				authResultBean.setFullApdu(desFireEV1Service.authenticate2(key, keyNo,keyType, DesfireUtils.hexStringToByteArray(result)));
				authResultBean.setSize(16);
				desfireFlowStep.authStep++;
				break;
			case 4:
				authResultBean.setFullApdu(desFireEV1Service.authenticate3(key, keyNo, keyType, DesfireUtils.hexStringToByteArray(result)));
				authResultBean.setSize(0);
				break;
			}
		return authResultBean;
	}


	public String decriptDesfireId(String apdu){
		byte[] resultByte = DesfireUtils.hexStringToByteArray(apdu);
		int length = Integer.parseInt(DesfireUtils.swapPairs(DesfireUtils.hexStringToByteArray(tempFileSize)), 16);
		byte[] resultDecript = desFireEV1Service.postprocess(resultByte, length, DESFireEV1Service.CommunicationSetting.ENCIPHERED);
		String hexaResult =  DesfireUtils.byteArrayToHexString(resultDecript);
		log.info("En hexa : " + hexaResult);
		log.info("En hexa : " + hexaResult.substring(0, length));
		return hexaResult;
	}

	public String decriptUid(String apdu){
		log.debug("APDU to decript : " + apdu);
		byte[] resultByte = DesfireUtils.hexStringToByteArray(apdu);
		byte[] resultDecript = desFireEV1Service.postprocess(resultByte, 7, DESFireEV1Service.CommunicationSetting.ENCIPHERED);
		String hexaResult =  DesfireUtils.byteArrayToHexString(resultDecript);
		log.info("En hexa : " + hexaResult);
		return hexaResult;
	}

	public String asBytesString(String desfireId) {
		int size = (int)(desfireId.length()/16)+1;
		desfireId = StringUtils.leftPad(desfireId, size*16, '0');
		String desfireIdAsBytesString = DesfireUtils.byteArrayToHexString(desfireId.getBytes());
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
		desfireId = "";
	}

	public static String leftPad(String originalString, int length,
		char padCharacter) {
		String paddedString = originalString;
		while (paddedString.length() < length) {
			paddedString = padCharacter + paddedString;
		}
		return paddedString;
	}

	public NfcResultBean readFreeMemory(String result) {
		reset();
		desfireFlowStep.action = Action.FREE_MEMORY;
		NfcResultBean freeMemoryResultBean = new NfcResultBean();
		freeMemoryResultBean.setCode(CODE.OK);
		if(result.isEmpty()) {
			freeMemoryResultBean.setFullApdu(desFireEV1Service.freeMemory());
		} else {
			freeMemoryResultBean.setFullApdu("END");
		}
		return freeMemoryResultBean;
	}

}
