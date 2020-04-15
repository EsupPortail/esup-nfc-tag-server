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

public class DesfireReadUidWithAuthActionService extends DesfireActionService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public DesfireReadUidWithAuthActionService(DesfireReadConfig desfireReadConfig, DesfireService desfireService,
			TagAuthService tagAuthService, LiveLongPoolController liveController) {
		super(desfireReadConfig, desfireService, tagAuthService, liveController);
	}

	
	@Override
	public FunctionType getFunction() {
		return FunctionType.READUIDWITHAUTH;
	}

	@Override
	NfcResultBean computeNfcResultBean(String result, String eppnInit, String cardId) {
		NfcResultBean nfcResultBean = desfireService.readUidWithAuth(result);
		return nfcResultBean;
	}
	
	@Override
	TagType getTagType() {
		return TagType.CSN;
	}
	
	@Override
	String getDesfireId(String result) {
		log.debug("UID  : " + result);
		String desfireId = desfireService.decriptUid(result);
		log.debug("UID descrypted  : " + desfireId);
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
		return result;
	}
}
