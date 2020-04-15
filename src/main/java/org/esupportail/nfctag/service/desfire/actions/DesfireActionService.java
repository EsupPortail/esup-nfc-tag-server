package org.esupportail.nfctag.service.desfire.actions;

import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.Action;
import org.esupportail.nfctag.beans.NfcResultBean.CODE;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.Response;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.DesfireUtils;
import org.esupportail.nfctag.web.live.LiveLongPoolController;

public abstract class DesfireActionService {
	
	public enum FunctionType{
		READUID, READUIDWITHAUTH, READ, WRITE, UPDATE
	}
	
	DesfireReadConfig desfireReadConfig;
	
	DesfireService desfireService;

	TagAuthService tagAuthService;
 
	LiveLongPoolController liveController;
	
	Boolean updateWrite = false;
	
	String desfireId;
	
	public DesfireActionService(DesfireReadConfig desfireReadConfig, DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
		this.desfireReadConfig = desfireReadConfig;
		this.desfireService = desfireService;
		this.tagAuthService = tagAuthService;
		this.liveController = liveController;
	}

	public NfcResultBean process(String eppnInit, Response response, String numeroId, String cardId, String result) {


		NfcResultBean nfcResultBean = computeNfcResultBean(result, eppnInit, cardId);
		if(Action.update.equals(nfcResultBean.getAction()) || Action.write.equals(nfcResultBean.getAction())){
			updateWrite = true;
		}

		if(!"END".equals(nfcResultBean.getFullApdu())) {
			String cmd = nfcResultBean.getFullApdu().substring(2, 4);
			nfcResultBean.setCmd(cmd);
			int nbParam = DesfireUtils.hexStringToByte(nfcResultBean.getFullApdu().substring(8, 10));
			nfcResultBean.setParam(nfcResultBean.getFullApdu().substring(10,10+(nbParam*2)));
		} else {

			nfcResultBean.setCode(CODE.END);
			nfcResultBean.setCmd("");
			nfcResultBean.setParam("");
			nfcResultBean.setSize(0);
			nfcResultBean.setAction(Action.none);
			desfireService.reset();
			
			result = overrideResultIfNeeded(result);
			
			if(result != ""){
				String msg = result.substring(result.length() - 2);
				response = DESFireEV1Service.Response.getResponse(Integer.parseInt(msg, 16));
			}
			if(DESFireEV1Service.Response.OPERATION_OK.equals(response)){
				TagType tagType = getTagType();
				desfireId = getDesfireId(result);
				String appName = getAppName();
				nfcResultBean.setAction(getAction(nfcResultBean.getAction()));
				boolean validate = true;


				if(nfcResultBean.getAction().equals(Action.none)){
					validate = false;
				}

				if(tagAuthService!=null) {
					TagLog tagLog = tagAuthService.auth(tagType, desfireId, numeroId, cardId, appName, validate);
					if(liveController!=null) {
						liveController.handleTagLog(tagLog);
					}
					nfcResultBean.setMsg(tagLog.getFirstname() + " " + tagLog.getLastname());
					nfcResultBean.setTaglogId(tagLog.getId());
				}

			} else {
				throw new EsupNfcTagException("Aucun encodage : " + response.toString(), numeroId);	
			}
		}

		return nfcResultBean;

	}

	abstract NfcResultBean computeNfcResultBean(String result, String eppnInit, String cardId);

	abstract Action getAction(Action action);

	abstract String getAppName();

	abstract String getDesfireId(String result);

	abstract TagType getTagType();
	
	abstract String overrideResultIfNeeded(String result);

	public abstract FunctionType getFunction();

	
	
}
