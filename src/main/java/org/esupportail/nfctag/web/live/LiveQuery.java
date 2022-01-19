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
package org.esupportail.nfctag.web.live;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;


public class LiveQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	String numeroId;
	
	Long authDateTimestamp;
	
	Date lastPollDate;

	public LiveQuery() {
		this.lastPollDate = new Date();
	}
	
	public LiveQuery(Long authDateTimestamp, String numeroId) {
		this.lastPollDate = new Date();
		this.numeroId = numeroId;
		this.authDateTimestamp = authDateTimestamp;
	}


    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getNumeroId() {
        return this.numeroId;
    }

    public void setNumeroId(String numeroId) {
        this.numeroId = numeroId;
    }

    public Long getAuthDateTimestamp() {
        return this.authDateTimestamp;
    }

    public void setAuthDateTimestamp(Long authDateTimestamp) {
        this.authDateTimestamp = authDateTimestamp;
    }

    public Date getLastPollDate() {
        return this.lastPollDate;
    }

    public void setLastPollDate(Date lastPollDate) {
        this.lastPollDate = lastPollDate;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LiveQuery)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        LiveQuery rhs = (LiveQuery) obj;
        return new EqualsBuilder().append(authDateTimestamp, rhs.authDateTimestamp).append(lastPollDate, rhs.lastPollDate).append(numeroId, rhs.numeroId).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(authDateTimestamp).append(lastPollDate).append(numeroId).toHashCode();
    }
}
