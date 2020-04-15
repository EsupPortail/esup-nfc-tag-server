package org.esupportail.nfctag.service.desfire.actions;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.Action;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.api.impl.DesfireReadDeuinfoConfig;
import org.esupportail.nfctag.service.api.impl.DesfireReadUidConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.KeyType;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.Response;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.DesfireUtils;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesfireDeuinfoActionService extends DesfireActionService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	enum Step{NONE, UID, ESCN, SIGNATURE, CERT, END, CHECK_KEY_ESCN, CHECK_KEY_UID};
	
	String escn = null;

	String signature;
	
	String cert;
	
	String uid = null;
	
	DesfireActionService currentDesfireActionService;
	
	DesfireReadDeuinfoConfig desfireReadDeuinfoConfig;
	
	Step step = Step.NONE;
	
	public DesfireDeuinfoActionService(DesfireReadConfig desfireReadConfig, DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
		super(desfireReadConfig, desfireService, tagAuthService, liveController);
		desfireReadDeuinfoConfig = (DesfireReadDeuinfoConfig)desfireReadConfig;
	}
	
	@Override
	public NfcResultBean process(String eppnInit, Response response, String numeroId, String cardId, String result) {
		NfcResultBean nfcResultBean = null;
		if(step == Step.NONE) {
			desfireReadConfig = new DesfireReadUidConfig();
			currentDesfireActionService = desfireReadConfig.getDesfireActionService(desfireService, null, null);
			step = Step.UID;
		}
		if(Step.UID.equals(step)) {
			nfcResultBean = currentDesfireActionService.process(eppnInit, response, numeroId, cardId, result);
			if(!StringUtils.isEmpty(currentDesfireActionService.desfireId)) {
				uid = currentDesfireActionService.desfireId;
				log.info("Got uid : " + uid);
				// now get escn
				desfireReadConfig = new DesfireReadConfig();
				desfireReadConfig.setDesfireAppId("F58840");
				desfireReadConfig.setDesfireFileNumber("00");
				currentDesfireActionService = desfireReadConfig.getDesfireActionService(desfireService, null, null);
				step = Step.ESCN;
				result = "";
			}
		} 
		if(Step.ESCN.equals(step)) {
			nfcResultBean = currentDesfireActionService.process(eppnInit, response, numeroId, cardId, result);
			if(!StringUtils.isEmpty(currentDesfireActionService.desfireId)) {
				escn = currentDesfireActionService.desfireId;				
				log.info("Got escn : " + escn);
				if(!StringUtils.isEmpty(desfireReadDeuinfoConfig.getBaseKey())) {
					// now check key escn
					step = Step.CHECK_KEY_ESCN;
					result = "";
				} else {
					log.warn("baseKey for deuinfo is not setup ; we didn't check auth on diversified keys");
					// now get signature
					desfireReadConfig = new DesfireReadConfig();
					desfireReadConfig.setDesfireAppId("F58840");
					desfireReadConfig.setDesfireFileNumber("01");
					currentDesfireActionService = desfireReadConfig.getDesfireActionService(desfireService, null, null);
					step = Step.SIGNATURE;
					result = "";
				}
			}
		}
		if(Step.CHECK_KEY_ESCN.equals(step)) {
			byte[] aid = DesfireUtils.hexStringToByteArray("F58840");
			byte keyNo = DesfireUtils.hexStringToByte("01");
			byte[] key = DesfireUtils.hexStringToByteArray(desfireReadDeuinfoConfig.getDesfireDiversifiedKey(escn));
			nfcResultBean = desfireService.authApp4ckeckKey(aid, result, key, keyNo, KeyType.AES);
			if(nfcResultBean.getFullApdu() == null) {			
				log.info("Auth with escn diversified key ok");
				// now check key uid
				step = Step.CHECK_KEY_UID;
				result = "";
			}
		}
		if(Step.CHECK_KEY_UID.equals(step)) {
			byte[] aid = DesfireUtils.hexStringToByteArray("F58840");
			byte keyNo = DesfireUtils.hexStringToByte("02");
			byte[] key = DesfireUtils.hexStringToByteArray(desfireReadDeuinfoConfig.getDesfireDiversifiedKey(uid));
			nfcResultBean = desfireService.authApp4ckeckKey(aid, result, key, keyNo, KeyType.AES);
			if(nfcResultBean.getFullApdu() == null) {
				log.info("Auth with uid diversified key ok");
				// now get signature
				desfireReadConfig = new DesfireReadConfig();
				desfireReadConfig.setDesfireAppId("F58840");
				desfireReadConfig.setDesfireFileNumber("01");
				currentDesfireActionService = desfireReadConfig.getDesfireActionService(desfireService, null, null);
				step = Step.SIGNATURE;
				result = "";
			}
		}
		if(Step.SIGNATURE.equals(step)) {
			nfcResultBean = currentDesfireActionService.process(eppnInit, response, numeroId, cardId, result);
			if(!StringUtils.isEmpty(currentDesfireActionService.desfireId)) {
				signature = currentDesfireActionService.desfireId;
				log.info("Got signature : " + signature);
				// now get certificat
				this.desfireReadConfig.setDesfireAppId("F58840");
				this.desfireReadConfig.setDesfireFileNumber("02");
				currentDesfireActionService = desfireReadConfig.getDesfireActionService(desfireService, null, null);
				step = Step.CERT;
				result = "";
			}
		}
		if(Step.CERT.equals(step)) {
			nfcResultBean = currentDesfireActionService.process(eppnInit, response, numeroId, cardId, result);
			if(!StringUtils.isEmpty(currentDesfireActionService.desfireId)) {
				cert = currentDesfireActionService.desfireId;
				log.info("Got cert : " + cert);
				step = Step.END;
			}
		}
		if(Step.END.equals(step)) {
			boolean validate = true;
			if(nfcResultBean.getAction().equals(Action.none)){
				validate = false;
			}
			if(tagAuthService!=null) {
				TagLog tagLog = tagAuthService.auth(TagType.DESFIRE, getDesfireId(), numeroId, cardId, "Deuinfo", validate);
				if(liveController!=null) {
					liveController.handleTagLog(tagLog);
				}
				nfcResultBean.setMsg(tagLog.getFirstname() + " " + tagLog.getLastname());
				nfcResultBean.setTaglogId(tagLog.getId());
			}
		}
		return nfcResultBean;
	}

	String getDesfireId() {
		return String.format("%s@%s@%s@%s", uid, escn, signature, cert);
	}
	
	@Override
	String getAppName() {
		return desfireReadConfig.getDesfireAppId();
	}

	@Override
	NfcResultBean computeNfcResultBean(String result, String eppnInit, String cardId) {
		return null;
	}

	@Override
	Action getAction(Action action) {
		return null;
	}

	@Override
	TagType getTagType() {
		return null;
	}

	@Override
	String overrideResultIfNeeded(String result) {
		return null;
	}

	@Override
	public FunctionType getFunction() {
		return null;
	}

	@Override
	String getDesfireId(String result) {
		return null;
	}
	
}

