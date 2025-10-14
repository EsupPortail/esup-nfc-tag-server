package org.esupportail.nfctag.dao;

import org.esupportail.nfctag.domain.AppliVersion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AppliVersionDao {

    @PersistenceContext
    transient EntityManager entityManager;

    public final EntityManager entityManager() {
        EntityManager em = entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public List<AppliVersion> findAllAppliVersions() {
        return entityManager().createQuery("SELECT o FROM AppliVersion o", AppliVersion.class).getResultList();
    }

    public AppliVersion findAppliVersion(Long id) {
        if (id == null) return null;
        return entityManager().find(AppliVersion.class, id);
    }

    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

    @Transactional
    public AppliVersion merge(AppliVersion appliVersion) {
        if (this.entityManager == null) this.entityManager = entityManager();
        AppliVersion merged = this.entityManager.merge(appliVersion);
        this.entityManager.flush();
        return merged;
    }

    @Transactional
    public void persist(AppliVersion appliVersion) {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(appliVersion);
    }

    @Transactional
    public void remove(Long id) {
        if (this.entityManager == null) this.entityManager = entityManager();
        AppliVersion attached = findAppliVersion(id);
        this.entityManager.remove(attached);
    }


}
