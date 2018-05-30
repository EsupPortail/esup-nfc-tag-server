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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Query;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { 
		"findTagLogsByCsnEquals", 
		"findTagLogsByDesfireIdEquals", 
		"findTagLogsByNumeroIdEquals",
		"findTagLogsByIdAndNumeroIdEquals",
		"findTagLogsByNumeroIdEqualsAndApplicationNameEqualsAndLocationEquals", 
		"findTagLogsByEppnInitLike", 
		"findTagLogsByCsnEquals", 
		"findTagLogsByLocationEquals", 
		"findTagLogsByEppnEquals", 
		"findTagLogsByApplicationNameEquals", 
		"findTagLogsByAuthDateBetween", 
		"findTagLogsByAuthDateGreaterThan", 
		"findTagLogsByAuthDateGreaterThanAndNumeroIdEquals", 
		"findTagLogsByAuthDateGreaterThanAndNumeroIdEqualsAndApplicationNameEqualsAndLocationEquals" 
		})

public class TagLog {

    public enum Status {

        none, valid, cancel
    }

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

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
    
    public static List<String> findYears() {
        EntityManager em = TagLog.entityManager();
        Query q = em.createNativeQuery("select trim(to_char(date_part('year', auth_date),'9999')) as year from tag_log group by year");
        return q.getResultList();
    }
    
}
