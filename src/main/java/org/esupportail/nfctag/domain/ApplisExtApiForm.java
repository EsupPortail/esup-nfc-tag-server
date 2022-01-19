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


public class ApplisExtApiForm {

	String appliExtKey;
	
	String appliExtDescription;
	
	String appliExtHeader;
	
	String appliExtBackgroundColor;

    public String getAppliExtKey() {
        return this.appliExtKey;
    }

    public void setAppliExtKey(String appliExtKey) {
        this.appliExtKey = appliExtKey;
    }

    public String getAppliExtDescription() {
        return this.appliExtDescription;
    }

    public void setAppliExtDescription(String appliExtDescription) {
        this.appliExtDescription = appliExtDescription;
    }

    public String getAppliExtHeader() {
        return this.appliExtHeader;
    }

    public void setAppliExtHeader(String appliExtHeader) {
        this.appliExtHeader = appliExtHeader;
    }

    public String getAppliExtBackgroundColor() {
        return this.appliExtBackgroundColor;
    }

    public void setAppliExtBackgroundColor(String appliExtBackgroundColor) {
        this.appliExtBackgroundColor = appliExtBackgroundColor;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
