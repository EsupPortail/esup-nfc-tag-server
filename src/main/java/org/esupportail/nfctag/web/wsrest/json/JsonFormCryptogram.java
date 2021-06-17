package org.esupportail.nfctag.web.wsrest.json;

import org.esupportail.nfctag.service.desfire.DesfireUtils;

public class JsonFormCryptogram {

    private String csn;

    private String damDefaultKey;

    private String damDefaultKeyVersion;

    private String aid;

    private String quotaLimit;

    private String keySetting1;

    private String keySetting2;

    private String isoDfId;

    private String isoDfName;

    public String getCsn() {
        return csn;
    }

    public void setCsn(String csn) {
        this.csn = csn;
    }

    public String getDamDefaultKey() {
        return damDefaultKey;
    }

    public void setDamDefaultKey(String damDefaultKey) {
        this.damDefaultKey = damDefaultKey;
    }

    public byte getDamDefaultKeyVersion() {
        return DesfireUtils.hexStringToByte(damDefaultKeyVersion);
    }

    public void setDamDefaultKeyVersion(String damDefaultKeyVersion) {
        this.damDefaultKeyVersion = damDefaultKeyVersion;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(String quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public byte getKeySetting1() {
        return DesfireUtils.hexStringToByte(keySetting1);
    }

    public void setKeySetting1(String keySetting1) {
        this.keySetting1 = keySetting1;
    }

    public byte getKeySetting2() {
        return DesfireUtils.hexStringToByte(keySetting2);
    }

    public void setKeySetting2(String keySetting2) {
        this.keySetting2 = keySetting2;
    }

    public String getIsoDfId() {
        return isoDfId;
    }

    public void setIsoDfId(String isoDfId) {
        this.isoDfId = isoDfId;
    }

    public String getIsoDfName() {
        return isoDfName;
    }

    public void setIsoDfName(String isoDfName) {
        this.isoDfName = isoDfName;
    }
}
