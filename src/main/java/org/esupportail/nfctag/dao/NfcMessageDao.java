package org.esupportail.nfctag.dao;

import org.esupportail.nfctag.domain.NfcMessage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class NfcMessageDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private NfcMessageDao nfcMessageDao;

    public final EntityManager entityManager() {
        EntityManager em = entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public NfcMessage findNfcMessage(Long id) {
        if (id == null) return null;
        return entityManager().find(NfcMessage.class, id);
    }

    @Transactional
    public NfcMessage merge(NfcMessage nfcMessage) {
        if (this.entityManager == null) this.entityManager = entityManager();
        NfcMessage merged = this.entityManager.merge(nfcMessage);
        this.entityManager.flush();
        return merged;
    }

    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

    @Transactional
    public void persist(NfcMessage nfcMessage) {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(nfcMessage);
    }

    @Transactional
    public void remove(Long id) {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            NfcMessage attached = nfcMessageDao.findNfcMessage(id);
            this.entityManager.remove(attached);
        }
    }

}
