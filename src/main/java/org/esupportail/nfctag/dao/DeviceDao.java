package org.esupportail.nfctag.dao;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.repositories.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DeviceDao {

    public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("numeroId", "validateAuthWoConfirmation", "eppnInit", "imei", "macAddress", "userAgent", "location", "createDate", "lastUseDate", "application", "lastPollDate");
    
    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    DeviceRepository deviceRepository;

    public TypedQuery<Device> findDevicesByLocationAndEppnInit(String location, String eppnInit) {
        if (location == null || location.length() == 0) throw new IllegalArgumentException("The location argument is required");
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        EntityManager em = entityManager;
        TypedQuery<Device> q = em.createQuery("SELECT o FROM Device AS o WHERE o.location = :location AND o.eppnInit = :eppnInit", Device.class);
        q.setParameter("location", location);
        q.setParameter("eppnInit", eppnInit);
        return q;
    }

    public TypedQuery<Device> findDevicesByEppnInitAndImeiEquals(String eppnInit, String imei) {
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        if (imei == null || imei.length() == 0) throw new IllegalArgumentException("The imei argument is required");
        EntityManager em = entityManager;
        TypedQuery<Device> q = em.createQuery("SELECT o FROM Device AS o WHERE o.eppnInit = :eppnInit AND o.imei = :imei", Device.class);
        q.setParameter("eppnInit", eppnInit);
        q.setParameter("imei", imei);
        return q;
    }

    public TypedQuery<Device> findDevicesByLocationAndEppnInitAndMacAddressEquals(String location, String eppnInit, String macAddress) {
        if (location == null || location.length() == 0) throw new IllegalArgumentException("The location argument is required");
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        if (macAddress == null || macAddress.length() == 0) throw new IllegalArgumentException("The macAddress argument is required");
        EntityManager em = entityManager;
        TypedQuery<Device> q = em.createQuery("SELECT o FROM Device AS o WHERE o.location = :location AND o.eppnInit = :eppnInit AND o.macAddress = :macAddress", Device.class);
        q.setParameter("location", location);
        q.setParameter("eppnInit", eppnInit);
        q.setParameter("macAddress", macAddress);
        return q;
    }

    public Long countFindDevicesByLocationAndEppnInit(String location, String eppnInit) {
        if (location == null || location.length() == 0) throw new IllegalArgumentException("The location argument is required");
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Device AS o WHERE o.location = :location AND o.eppnInit = :eppnInit", Long.class);
        q.setParameter("location", location);
        q.setParameter("eppnInit", eppnInit);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<Device> findDevicesByNumeroIdEquals(String numeroId) {
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        EntityManager em = entityManager;
        TypedQuery<Device> q = em.createQuery("SELECT o FROM Device AS o WHERE o.numeroId = :numeroId", Device.class);
        q.setParameter("numeroId", numeroId);
        return q;
    }

    public Long countFindDevicesByLocationAndEppnInitAndMacAddressEquals(String location, String eppnInit, String macAddress) {
        if (location == null || location.length() == 0) throw new IllegalArgumentException("The location argument is required");
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        if (macAddress == null || macAddress.length() == 0) throw new IllegalArgumentException("The macAddress argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Device AS o WHERE o.location = :location AND o.eppnInit = :eppnInit AND o.macAddress = :macAddress", Long.class);
        q.setParameter("location", location);
        q.setParameter("eppnInit", eppnInit);
        q.setParameter("macAddress", macAddress);
        return ((Long) q.getSingleResult());
    }

    public Long countFindDevicesByNumeroIdEquals(String numeroId) {
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Device AS o WHERE o.numeroId = :numeroId", Long.class);
        q.setParameter("numeroId", numeroId);
        return ((Long) q.getSingleResult());
    }

    public final EntityManager entityManager() {
        EntityManager em = entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public long countDevices() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Device o", Long.class).getSingleResult();
    }

    public List<Device> findAllDevices() {
        return entityManager().createQuery("SELECT o FROM Device o", Device.class).getResultList();
    }

    public Device findDevice(Long id) {
        if (id == null) return null;
        return entityManager().find(Device.class, id);
    }

    public Page<Device> findDeviceEntries(Application application, String searchText, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Device> query = criteriaBuilder.createQuery(Device.class);
        Root<Device> c = query.from(Device.class);
        final List<Predicate> predicates = new ArrayList<>();
        if(application != null && application.getId() != null) {
            predicates.add(criteriaBuilder.equal(c.get("application"), application));
        }

        if(!StringUtils.isEmpty(searchText)) {
            Expression<Boolean> fullTestSearchExpression = criteriaBuilder.function("fts", Boolean.class, criteriaBuilder.literal("'" + searchText + "'"));
            Expression<Double> fullTestSearchRanking = criteriaBuilder.function("ts_rank", Double.class, criteriaBuilder.literal("'" + searchText + "'"));
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
        }

        Specification<Device> spec = (root, q, cb) -> {
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return deviceRepository.findAll(spec, pageable);
    }


    @Transactional
    public Device merge(Device device) {
        if (this.entityManager == null) this.entityManager = entityManager();
        Device merged = this.entityManager.merge(device);
        this.entityManager.flush();
        return merged;
    }

    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

    @Transactional
    public void persist(Device device) {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(device);
    }

    @Transactional
    public void remove(Long id) {
        if (this.entityManager == null) this.entityManager = entityManager();
        Device attached = findDevice(id);
        this.entityManager.remove(attached);
    }

    public List<Device> findDevicesByApplication(Application application) {
        if (application == null) throw new IllegalArgumentException("The application argument is required");
        EntityManager em = entityManager;
        TypedQuery<Device> q = em.createQuery("SELECT o FROM Device AS o WHERE o.application = :application", Device.class);
        q.setParameter("application", application);
        return q.getResultList();
    }
}
