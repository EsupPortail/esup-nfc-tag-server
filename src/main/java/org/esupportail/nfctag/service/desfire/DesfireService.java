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

import java.io.UnsupportedEncodingException;

import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.api.impl.DesfireAuthConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nfcjlib.core.util.Dump;

public class DesfireService {
	
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
	private DesfireAuthSession desfireAuthSession;
	
	private DESFireEV1Service desFireEV1Service;

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
	
	public String getIDP2sAES(int step, String result){
		String apdu = "";
		KeyType keyType = KeyType.AES;
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireAuthSession.getDesfireAuthConfig().getDesfireAppId());
		byte keyNo = desFireEV1Service.hexStringToByte(desfireAuthSession.getDesfireAuthConfig().getDesfireKeyNumber());
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireAuthSession.getDesfireAuthConfig().getDesfireKey());
		byte[] payload = desFireEV1Service.hexStringToByteArray(desfireAuthSession.getDesfireAuthConfig().getReadFilePayload());
		switch(step){
		case 1:
			apdu = desFireEV1Service.selectApplication(desFireEV1Service.swapPairsByte(aid));
			break;
		case 2:
			apdu = desFireEV1Service.authenticate1(key,keyNo, keyType);
			break;
		case 3:
			apdu = desFireEV1Service.authenticate2(key, keyNo,keyType,desFireEV1Service.hexStringToByteArray(result));
			break;
		case 4:
			apdu = desFireEV1Service.authenticate3(key, keyNo, keyType, desFireEV1Service.hexStringToByteArray(result));
			break;
		case 5:
			apdu = desFireEV1Service.readData(payload);
			break;
		case 6:
			apdu = "OK";
			break;
		}
		return apdu;
	}

	public String getUidAES(int step, String result){
		String apdu = "";
		KeyType keyType = KeyType.AES;
		byte[] aid = desFireEV1Service.hexStringToByteArray(desfireAuthSession.getDesfireAuthConfig().getDesfireAppId());
		byte keyNo = desFireEV1Service.hexStringToByte(desfireAuthSession.getDesfireAuthConfig().getDesfireKeyNumber());
		byte[] key = desFireEV1Service.hexStringToByteArray(desfireAuthSession.getDesfireAuthConfig().getDesfireKey());
		byte[] payload = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x00, (byte) 0x00};
		switch(step){
		case 1:
			apdu = desFireEV1Service.authenticate1(key,keyNo, keyType);
			break;
		case 2:
			apdu = desFireEV1Service.authenticate2(key, keyNo,keyType,desFireEV1Service.hexStringToByteArray(result));
			break;
		case 3:
			apdu = desFireEV1Service.authenticate3(key, keyNo, keyType, desFireEV1Service.hexStringToByteArray(result));
			break;
		case 4:
			apdu = desFireEV1Service.getCardUID();
			break;
		case 5:
			apdu = "OK";
			break;
		}
		return apdu;
	}
	
	public String decriptIDP2S(String apdu){
		byte[] resultByte = desFireEV1Service.hexStringToByteArray(apdu);
		byte[] resultDecript = desFireEV1Service.postprocess(resultByte, 22, DESFireEV1Service.CommunicationSetting.ENCIPHERED);
		String idp2s = "";
		try {
			idp2s = new String(resultDecript, "UTF-8");
			idp2s = idp2s.substring(1, 16);
			System.err.println(idp2s);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return idp2s;
	}
	
	public void setNumeroId(String numeroId) throws EsupNfcTagException {
		String nfcAuthConfigKey = applicationsService.getApplicationFromNumeroId(numeroId).getNfcConfig();
		DesfireAuthConfig desfireAuthConfig = (DesfireAuthConfig)nfcAuthConfigService.get(nfcAuthConfigKey);
		desfireAuthSession.setDesfireAuthConfig(desfireAuthConfig);;
	}
}
