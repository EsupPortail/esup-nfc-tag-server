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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Configurable

public class Application {

    @NotNull
    private String name;

    @NotNull
    private String nfcConfig;

    @NotNull
    private String appliExt;

    @NotNull
    private String tagIdCheck;

    private String description;
    
    private boolean active = true;
    
    @Column
    private Boolean sgcClientApp = false;
    
    private Boolean validateAuthWoConfirmationDefault = false;
    
    private Boolean displayAppNameBlock = false;
    
    @Transient
    private List<String> locations = new ArrayList<String>();
    
    @Transient
    private Boolean available = true;
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

    public String getNfcConfig() {
        return this.nfcConfig;
    }

    public String getTagIdCheck() {
        return this.tagIdCheck;
    }

    public void setNfcConfig(String nfcConfig) {
        this.nfcConfig = nfcConfig;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public void setValidateAuthWoConfirmationDefault(Boolean validateAuthWoConfirmationDefault) {
        this.validateAuthWoConfirmationDefault = validateAuthWoConfirmationDefault;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return this.available;
    }

    public void setDisplayAppNameBlock(Boolean displayAppNameBlock) {
        this.displayAppNameBlock = displayAppNameBlock;
    }

    public String getAppliExt() {
        return this.appliExt;
    }

    public void setTagIdCheck(String tagIdCheck) {
        this.tagIdCheck = tagIdCheck;
    }

    public Boolean getDisplayAppNameBlock() {
        return this.displayAppNameBlock;
    }

    public Boolean getSgcClientApp() {
        return this.sgcClientApp;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getLocations() {
        return this.locations;
    }

    public void setSgcClientApp(Boolean sgcClientApp) {
        this.sgcClientApp = sgcClientApp;
    }

    public void setAppliExt(String appliExt) {
        this.appliExt = appliExt;
    }

    public String getName() {
        return this.name;
    }

    public Boolean getValidateAuthWoConfirmationDefault() {
        return this.validateAuthWoConfirmationDefault;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getNfcConfigDesc() {
//        NfcAuthConfig nfcAuthConfig = nfcAuthConfigService.get(this.nfcConfig);
//        if (nfcAuthConfig != null) {
//            return nfcAuthConfig.getDescription();
//        } else {
//            return null;
//        }
        return nfcConfig;
    }

    public String getAppliExtDesc() {
//        AppliExtApi extApi = applisExtService.get(this.appliExt);
//        if (extApi != null) {
//            return extApi.getDescription();
//        } else {
//            return null;
//        }
        return appliExt;
    }

    public String getTagIdCheckDesc() {
//        TagIdCheckApi tagIdCheckApi = tagIdCheckService.get(this.tagIdCheck);
//        if (tagIdCheckApi != null) {
//            return tagIdCheckApi.getDescription();
//        } else {
//            return null;
//        }
        return tagIdCheck;
    }

}
