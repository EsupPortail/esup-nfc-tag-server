package org.esupportail.nfctag.dao;

import org.esupportail.nfctag.domain.Application;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class ApplicationDao {

    public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("applisExtService", "nfcAuthConfigService", "tagIdCheckService", "name", "nfcConfig", "appliExt", "tagIdCheck", "description", "active", "sgcClientApp", "validateAuthWoConfirmationDefault", "displayAppNameBlock", "locations", "available");

    @PersistenceContext
    private EntityManager entityManager;

    public TypedQuery<Application> findApplicationsByNameEquals(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = entityManager;
        TypedQuery<Application> q = em.createQuery("SELECT o FROM Application AS o WHERE o.name = :name", Application.class);
        q.setParameter("name", name);
        return q;
    }

    public TypedQuery<Application> findApplicationsBySgcClientApp(Boolean sgcClientApp) {
        if (sgcClientApp == null) throw new IllegalArgumentException("The sgcClientApp argument is required");
        EntityManager em = entityManager;
        TypedQuery<Application> q = em.createQuery("SELECT o FROM Application AS o WHERE o.sgcClientApp = :sgcClientApp", Application.class);
        q.setParameter("sgcClientApp", sgcClientApp);
        return q;
    }

    public List<Application> findAllApplications() {
        return entityManager().createQuery("SELECT o FROM Application o", Application.class).getResultList();
    }

    public final EntityManager entityManager() {
        EntityManager em = entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public long countApplications() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Application o", Long.class).getSingleResult();
    }

    public List<Application> findAllApplications(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Application o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Application.class).getResultList();
    }

    public Application findApplication(Long id) {
        if (id == null) return null;
        return entityManager().find(Application.class, id);
    }

    public List<Application> findApplicationEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Application o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Application.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void remove(Long id) {
        if (this.entityManager == null) this.entityManager = entityManager();
        Application attached = findApplication(id);
        this.entityManager.remove(attached);
    }

    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

    @Transactional
    public Application merge(Application application) {
        if (this.entityManager == null) this.entityManager = entityManager();
        Application merged = entityManager.merge(application);
        this.entityManager.flush();
        return merged;
    }

    @Transactional
    public void persist(Application application) {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(application);
    }
}
