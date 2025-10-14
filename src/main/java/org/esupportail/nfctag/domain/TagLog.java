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
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Configurable
@Table(indexes = {
        @Index(name = "tag_log_mm_idx", columnList = "authDate,numeroId,applicationName,location"),
})
public class TagLog {

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

    public void setDesfireId(String desfireId) {
        this.desfireId = desfireId;
    }

    public String getDesfireId() {
        return this.desfireId;
    }

    public String getCsn() {
        return this.csn;
    }

    public void setAuthDate(Date authDate) {
        this.authDate = authDate;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getNumeroId() {
        return this.numeroId;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getAuthDate() {
        return this.authDate;
    }

    public void setCsn(String csn) {
        this.csn = csn;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setNumeroId(String numeroId) {
        this.numeroId = numeroId;
    }

    public String getEppn() {
        return this.eppn;
    }

    public void setEppn(String eppn) {
        this.eppn = eppn;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLocation() {
        return this.location;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getEppnInit() {
        return this.eppnInit;
    }

    public void setEppnInit(String eppnInit) {
        this.eppnInit = eppnInit;
    }

    public enum Status {

        none, valid, cancel
    }

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

    @Column(columnDefinition="TEXT")
    private String desfireId;

    private String csn;

    private String eppn;

    private String firstname;

    private String lastname;

    private String numeroId;

    private String eppnInit;

    private String applicationName;

    private String location;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Status liveStatus;
    
    @DateTimeFormat(style = "MM")
    private Date authDate;

    public String getAuthDateString() {
        return dateFormatter.format(authDate);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    
    public Status getLiveStatus() {
        return this.liveStatus;
    }
    
    public void setLiveStatus(Status liveStatus) {
        this.liveStatus = liveStatus;
    }

}
