package org.esupportail.nfctag.domain;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;

@Entity
@Configurable
public class AppliVersion {

        @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    String esupNfcTagVersion;

    public String getEsupNfcTagVersion() {
        return this.esupNfcTagVersion;
    }

    public void setEsupNfcTagVersion(String esupNfcTagVersion) {
        this.esupNfcTagVersion = esupNfcTagVersion;
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

       public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
