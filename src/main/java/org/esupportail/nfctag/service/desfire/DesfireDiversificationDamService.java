package org.esupportail.nfctag.service.desfire;

import org.esupportail.nfctag.service.api.TagWriteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DesfireDiversificationDamService implements InitializingBean {

    protected final static Logger log = LoggerFactory.getLogger(DesfireDiversificationDamService.class);

    TagWriteApi tagWriteApi;

    DesfireDiversification desfireDiversification = new DesfireDiversification();

    public void setTagWriteApi(TagWriteApi tagWriteApi) {
        this.tagWriteApi = tagWriteApi;
    }

    public byte[] getDamAuthKey(String csn) throws Exception {
        byte[] cardUid = DesfireUtils.hexStringToByteArray(csn);
        
        String diversDamKey;
        diversDamKey = tagWriteApi.getDiversDamKey(csn);
        if (diversDamKey == null) {
            return new byte[16];
        }
        return desfireDiversification.diversificationAES128(DesfireUtils.hexStringToByteArray(diversDamKey), cardUid, cardUid.length);
    }

    public byte[] getDamEncKey(String csn) throws Exception {
        byte[] cardUid = DesfireUtils.hexStringToByteArray(csn);
        cardUid[0] = (byte) 0x00;

        String diversDamKey;
        diversDamKey = tagWriteApi.getDiversDamKey(csn);
        if (diversDamKey == null) {
            return new byte[16];
        }
        return desfireDiversification.diversificationAES128(DesfireUtils.hexStringToByteArray(diversDamKey), cardUid, cardUid.length);
    }

    public byte[] getDamMacKey(String csn) throws Exception {
        byte[] cardUid = DesfireUtils.hexStringToByteArray(csn);
        cardUid[0] = (byte) 0xFF;

        String diversDamKey;
        diversDamKey = tagWriteApi.getDiversDamKey(csn);
        if (diversDamKey == null) {
            return new byte[16];
        }
        return desfireDiversification.diversificationAES128(DesfireUtils.hexStringToByteArray(diversDamKey), cardUid, cardUid.length);
    }

    public String createDamBaseKey(String csn) {
        return tagWriteApi.createDiversDamKey(csn);
    }

    public String resetDamBaseKey(String csn) {
        return tagWriteApi.resetDiversDamKey(csn);
    }

    @Override
    public void afterPropertiesSet() {
        if (this.tagWriteApi == null) {
            throw new RuntimeException("WS url must be set");
        }
    }
}
