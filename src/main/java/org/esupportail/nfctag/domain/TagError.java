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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Configurable
@Entity

public class TagError {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("exception", "errorDate", "numeroId");
    @PersistenceContext
    transient EntityManager entityManager;
    private Exception exception;

    @DateTimeFormat(style = "MM")
    private Date errorDate;
    
    private String numeroId;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public TagError() {
	}
    
	public TagError(Exception exception) {
		this.exception = exception;
		this.errorDate = new Date();
	}

    public static final EntityManager entityManager() {
        EntityManager em = new TagError().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public static long countTagErrors() {
        return entityManager().createQuery("SELECT COUNT(o) FROM TagError o", Long.class).getSingleResult();
    }

    public static List<TagError> findAllTagErrors() {
        return entityManager().createQuery("SELECT o FROM TagError o", TagError.class).getResultList();
    }

    public static List<TagError> findAllTagErrors(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM TagError o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, TagError.class).getResultList();
    }

    public static TagError findTagError(Long id) {
        if (id == null) return null;
        return entityManager().find(TagError.class, id);
    }

    public static List<TagError> findTagErrorEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM TagError o", TagError.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<TagError> findTagErrorEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM TagError o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, TagError.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public Exception getException() {
        return this.exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Date getErrorDate() {
        return this.errorDate;
    }

    public void setErrorDate(Date errorDate) {
        this.errorDate = errorDate;
    }

    public String getNumeroId() {
        return this.numeroId;
    }

    public void setNumeroId(String numeroId) {
        this.numeroId = numeroId;
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

    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

    @Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

    @Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            TagError attached = TagError.findTagError(this.id);
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public TagError merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        TagError merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
