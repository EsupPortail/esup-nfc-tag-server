/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.domain;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Configurable

public class Device {

    @Column(name = "numero_id")
    private String numeroId;

    @Column(name = "validate_auth_wo_confirmation")
    private boolean validateAuthWoConfirmation = false;

    @Column(name = "eppn_init")
    private String eppnInit;
    
    private String imei;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "user_agent")
    private String userAgent;
    
    private String location;
    
    @DateTimeFormat(style = "MM")
    @Column(name = "create_date")
    private Date createDate;
    
    @DateTimeFormat(style = "MM")
    @Column(name = "last_use_date")
    private Date lastUseDate;
    
    @ManyToOne
    @JoinColumn(name = "application")
    private Application application;
    
    @Transient
    private Date lastPollDate;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    public String getApplicationName(){
    	return this.getApplication()!=null ? this.getApplication().getName() : "";
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNumeroId() {
        return this.numeroId;
    }

    public void setNumeroId(String numeroId) {
        this.numeroId = numeroId;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public Date getLastUseDate() {
        return this.lastUseDate;
    }

    public boolean isValidateAuthWoConfirmation() {
        return this.validateAuthWoConfirmation;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setValidateAuthWoConfirmation(boolean validateAuthWoConfirmation) {
        this.validateAuthWoConfirmation = validateAuthWoConfirmation;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Application getApplication() {
        return this.application;
    }

    public void setLastPollDate(Date lastPollDate) {
        this.lastPollDate = lastPollDate;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getEppnInit() {
        return this.eppnInit;
    }

    public void setEppnInit(String eppnInit) {
        this.eppnInit = eppnInit;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setLastUseDate(Date lastUseDate) {
        this.lastUseDate = lastUseDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastPollDate() {
        return this.lastPollDate;
    }

}
