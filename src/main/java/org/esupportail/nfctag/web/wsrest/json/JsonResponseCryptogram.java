package org.esupportail.nfctag.web.wsrest.json;

public class JsonResponseCryptogram {

    private String result;

    private int damSlotNO;

    private byte damSlotVersion;

    private String encK;

    private String dammac;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getDamSlotNO() {
        return damSlotNO;
    }

    public void setDamSlotNO(int damSlotNO) {
        this.damSlotNO = damSlotNO;
    }

    public byte getDamSlotVersion() {
        return damSlotVersion;
    }

    public void setDamSlotVersion(byte damSlotVersion) {
        this.damSlotVersion = damSlotVersion;
    }

    public String getEncK() {
        return encK;
    }

    public void setEncK(String encK) {
        this.encK = encK;
    }

    public String getDammac() {
        return dammac;
    }

    public void setDammac(String dammac) {
        this.dammac = dammac;
    }
}
