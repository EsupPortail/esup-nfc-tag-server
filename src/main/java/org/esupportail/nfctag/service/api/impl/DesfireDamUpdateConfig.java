package org.esupportail.nfctag.service.api.impl;

import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.actions.DesfireActionService;
import org.esupportail.nfctag.service.desfire.actions.DesfireDamUpdateActionService;
import org.esupportail.nfctag.web.live.LiveLongPoolController;

public class DesfireDamUpdateConfig extends DesfireWriteConfig {

    @Override
    public DesfireActionService getDesfireActionService(DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
        return new DesfireDamUpdateActionService(null, desfireService, tagAuthService, liveController);
    }

}
