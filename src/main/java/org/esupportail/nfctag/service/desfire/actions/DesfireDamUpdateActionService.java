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

public class DesfireDamUpdateActionService extends DesfireActionService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public DesfireDamUpdateActionService(DesfireReadConfig desfireReadConfig, DesfireService desfireService,
                                      TagAuthService tagAuthService, LiveLongPoolController liveController) {
        super(desfireReadConfig, desfireService, tagAuthService, liveController);
    }

    @Override
    public FunctionType getFunction() {
        return FunctionType.UPDATE;
    }

    @Override
    NfcResultBean computeNfcResultBean(String result, String eppnInit, String cardId) {
        NfcResultBean nfcResultBean = desfireService.damUpdateCard(result, eppnInit, cardId);
        return nfcResultBean;
    }

    @Override
    TagType getTagType() {
        return TagType.CSN;
    }

    @Override
    String getDesfireId(String result) {
        return "";
    }

    @Override
    String getAppName() {
        return "";
    }

    @Override
    NfcResultBean.Action getAction(Action action) {
        if(updateWrite) {
            return Action.update;
        }
        return action;
    }

    @Override
    String overrideResultIfNeeded(String result) {
        return result;
    }

}
