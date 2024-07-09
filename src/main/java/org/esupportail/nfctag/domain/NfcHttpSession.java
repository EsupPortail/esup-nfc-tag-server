package org.esupportail.nfctag.domain;

import java.util.Date;

public class NfcHttpSession {
    Date createdDate;

    String sessionId;

    String remoteIp;

    String originRequestUri;

    String userEppn;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getOriginRequestUri() {
        return originRequestUri;
    }

    public void setOriginRequestUri(String originRequestUri) {
        this.originRequestUri = originRequestUri;
    }

    public String getUserEppn() {
        return userEppn;
    }

    public void setUserEppn(String userEppn) {
        this.userEppn = userEppn;
    }
}
