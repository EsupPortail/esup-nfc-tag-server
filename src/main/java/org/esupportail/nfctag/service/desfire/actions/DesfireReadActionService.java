package org.esupportail.nfctag.service.desfire.actions;

import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.Action;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesfireReadActionService extends DesfireActionService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public DesfireReadActionService(DesfireReadConfig desfireReadConfig, DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
		super(desfireReadConfig, desfireService, tagAuthService, liveController);
	}
	
	@Override
	public FunctionType getFunction() {
		return FunctionType.READ;
	}

	@Override
	NfcResultBean computeNfcResultBean(String result, String eppnInit, String cardId) {
		NfcResultBean nfcResultBean;
		if(desfireReadConfig.getDesfireKey() == null) {
			nfcResultBean = desfireService.readDesfireIdNoAuth(desfireReadConfig, result);
		} else {
			nfcResultBean = desfireService.readDesfireId(desfireReadConfig, result);
		}
		return nfcResultBean;
	}
	
	@Override
	TagType getTagType() {
		return TagType.DESFIRE;
	}
	
	@Override
	String getDesfireId(String result) {
		String desfireId = result;
		if(desfireReadConfig.getDesfireKey() != null) {
			log.debug("desfireId crypted  : " + result);
			desfireId = desfireService.decriptDesfireId(result);
			log.debug("desfireId descrypted  : " + desfireId);
		} else {
			// delete 4 last chars (9100)
			if(result.length()>4) {
				desfireId = result.substring(0, result.length()-4);
			}
		}
		return desfireId;
	}
	
	@Override
	String getAppName() {
		return desfireReadConfig.getDesfireAppName();
	}
	
	@Override
	Action getAction(Action action) {
		return Action.read;
	}
	
	@Override
	String overrideResultIfNeeded(String result) {
		return desfireService.tempRead;
	}

}
