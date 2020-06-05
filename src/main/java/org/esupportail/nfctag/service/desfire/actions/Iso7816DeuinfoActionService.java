package org.esupportail.nfctag.service.desfire.actions;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.beans.NfcResultBean.Action;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.api.TagIdCheckApi.TagType;
import org.esupportail.nfctag.service.api.impl.DesfireReadConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service.Response;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iso7816DeuinfoActionService extends DesfireActionService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	enum Step{NONE, UID, SELECT_DEUINFO_APP, SELECT_ESCN, READ_ESCN, SELECT_SIGNATURE, READ_SIGNATURE, SELECT_CERT, READ_CERT_1, READ_CERT_2, READ_CERT_3, END};
	
	String escn = null;

	String signature;
	
	String cert;
	
	String uid = null;
	
	Step step = Step.NONE;
	
	public Iso7816DeuinfoActionService(DesfireReadConfig desfireReadConfig, DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
		super(desfireReadConfig, desfireService, tagAuthService, liveController);
	}
	
	@Override
	public NfcResultBean process(String eppnInit, Response response, String numeroId, String cardId, String result) {
		
		log.debug(String.format("Previous Result : %s", result));
		if(!StringUtils.isEmpty(result) && result.length()>4) {
			// remove 9000 at the end
			result = result.substring(0, result.length() - 4);
		}

		NfcResultBean nfcResultBean = null;
		if(step == Step.NONE) {
			step = Step.UID;
		}
		if(Step.UID.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "FFCA000000");
			if(!StringUtils.isEmpty(result)) {
				uid = result;
				result = "";
				step = Step.SELECT_DEUINFO_APP;
			}
		} 
		if(Step.SELECT_DEUINFO_APP.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00A4040009A00000061404F5884000");
			if(!StringUtils.isEmpty(result)) {
				result = "";
				step = Step.SELECT_ESCN;
			}
		} 
		if(Step.SELECT_ESCN.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00A40000021001");
			if(!StringUtils.isEmpty(result)) {
				result = "";
				step = Step.READ_ESCN;
			}
		}
		if(Step.READ_ESCN.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00B0000000");
			if(!StringUtils.isEmpty(result)) {
				escn = result;
				result = "";
				step = Step.SELECT_SIGNATURE;
			}
		}
		if(Step.SELECT_SIGNATURE.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00A40000021002");
			if(!StringUtils.isEmpty(result)) {
				result = "";
				step = Step.READ_SIGNATURE;
			}
		}
		if(Step.READ_SIGNATURE.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00B0000000");
			if(!StringUtils.isEmpty(result)) {
				signature = result;
				result = "";
				step = Step.SELECT_CERT;
			}
		}
		if(Step.SELECT_CERT.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00A40000021003");
			if(!StringUtils.isEmpty(result)) {
				result = "";
				step = Step.READ_CERT_1;
			}
		}
		if(Step.READ_CERT_1.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00B0000000");
			if(!StringUtils.isEmpty(result)) {
				cert = result;
				result = "";
				step = Step.READ_CERT_2;
			}
		}
		if(Step.READ_CERT_2.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00B0010000");
			if(!StringUtils.isEmpty(result)) {
				cert += result;
				result = "";
				step = Step.READ_CERT_3;
			}
		}
		if(Step.READ_CERT_3.equals(step)) {
			nfcResultBean = desfireService.callApdu(result, "00B0020000");
			if(!StringUtils.isEmpty(result)) {
				cert += result;
				result = "";
				step = Step.END;
			}
		}
		if(Step.END.equals(step)) {
			boolean validate = nfcResultBean==null || Action.none.equals(nfcResultBean.getAction());
			if(tagAuthService!=null) {
				TagLog tagLog = tagAuthService.auth(TagType.DESFIRE, getDesfireId(), numeroId, cardId, "Deuinfo", validate);
				if(liveController!=null) {
					liveController.handleTagLog(tagLog);
				}
				nfcResultBean.setMsg(tagLog.getFirstname() + " " + tagLog.getLastname());
				nfcResultBean.setTaglogId(tagLog.getId());
			}
			desfireService.reset();
		}
		
		log.debug(String.format("Next Apdu : %s", nfcResultBean.getFullApdu()));
		
		return nfcResultBean;
	}

	String getDesfireId() {
		return String.format("%s@%s@%s@%s", uid, escn, signature, cert);
	}
	
	@Override
	String getAppName() {
		return "Check Iso7816 Deuinfo";
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

