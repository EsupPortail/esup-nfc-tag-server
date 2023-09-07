package org.esupportail.nfctag.dao;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.domain.TagLog;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class TagLogDao {

    public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("dateFormatter", "desfireId", "csn", "eppn", "firstname", "lastname", "numeroId", "eppnInit", "applicationName", "location", "status", "liveStatus", "authDate");

    @PersistenceContext
    private EntityManager entityManager;

    public final EntityManager entityManager() {
        EntityManager em = entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public long countTagLogs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM TagLog o", Long.class).getSingleResult();
    }

    public TagLog findTagLog(Long id) {
        if (id == null) return null;
        return entityManager().find(TagLog.class, id);
    }

    public List<TagLog> findTagLogEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM TagLog o", TagLog.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public List<TagLog> findTagLogEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM TagLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, TagLog.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public Long countFindTagLogsThisDay() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery("SELECT COUNT(*) FROM tag_log where date_trunc('day', auth_date) = date_trunc('day', now())");
        return ((BigInteger) q.getSingleResult()).longValue();
    }

    public TypedQuery<TagLog> findTagLogsByAuthDateGreaterThanAndNumeroIdEqualsAndApplicationNameEqualsAndLocationEquals(Date authDate, String numeroId, String applicationName, String location, String sortFieldName, String sortOrder) {
        if (authDate == null) throw new IllegalArgumentException("The authDate argument is required");
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        if (applicationName == null || applicationName.length() == 0) throw new IllegalArgumentException("The applicationName argument is required");
        if (location == null || location.length() == 0) throw new IllegalArgumentException("The location argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM TagLog AS o WHERE o.authDate > :authDate  AND o.numeroId = :numeroId  AND o.applicationName = :applicationName  AND o.location = :location");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<TagLog> q = em.createQuery(queryBuilder.toString(), TagLog.class);
        q.setParameter("authDate", authDate);
        q.setParameter("numeroId", numeroId);
        q.setParameter("applicationName", applicationName);
        q.setParameter("location", location);
        return q;
    }

    public TypedQuery<TagLog> findTagLogsByAuthDateGreaterThan(Date authDate, String sortFieldName, String sortOrder) {
        if (authDate == null) throw new IllegalArgumentException("The authDate argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM TagLog AS o WHERE o.authDate > :authDate");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<TagLog> q = em.createQuery(queryBuilder.toString(), TagLog.class);
        q.setParameter("authDate", authDate);
        return q;
    }

    public TypedQuery<TagLog> findTagLogsByIdAndNumeroIdEquals(Long id, String numeroId) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        EntityManager em = entityManager;
        TypedQuery<TagLog> q = em.createQuery("SELECT o FROM TagLog AS o WHERE o.id = :id AND o.numeroId = :numeroId", TagLog.class);
        q.setParameter("id", id);
        q.setParameter("numeroId", numeroId);
        return q;
    }

    public TypedQuery<TagLog> findTagLogsByAuthDateGreaterThanAndNumeroIdEquals(Date authDate, String numeroId, String sortFieldName, String sortOrder) {
        if (authDate == null) throw new IllegalArgumentException("The authDate argument is required");
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM TagLog AS o WHERE o.authDate > :authDate  AND o.numeroId = :numeroId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<TagLog> q = em.createQuery(queryBuilder.toString(), TagLog.class);
        q.setParameter("authDate", authDate);
        q.setParameter("numeroId", numeroId);
        return q;
    }

    public TypedQuery<TagLog> findTagLogsByNumeroIdEquals(String numeroId, String sortFieldName, String sortOrder) {
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM TagLog AS o WHERE o.numeroId = :numeroId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<TagLog> q = em.createQuery(queryBuilder.toString(), TagLog.class);
        q.setParameter("numeroId", numeroId);
        return q;
    }

    public TypedQuery<TagLog> findTagLogsByNumeroIdEqualsAndApplicationNameEqualsAndLocationEquals(String numeroId, String applicationName, String location, String sortFieldName, String sortOrder) {
        if (numeroId == null || numeroId.length() == 0) throw new IllegalArgumentException("The numeroId argument is required");
        if (applicationName == null || applicationName.length() == 0) throw new IllegalArgumentException("The applicationName argument is required");
        if (location == null || location.length() == 0) throw new IllegalArgumentException("The location argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM TagLog AS o WHERE o.numeroId = :numeroId  AND o.applicationName = :applicationName  AND o.location = :location");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<TagLog> q = em.createQuery(queryBuilder.toString(), TagLog.class);
        q.setParameter("numeroId", numeroId);
        q.setParameter("applicationName", applicationName);
        q.setParameter("location", location);
        return q;
    }

    @Transactional
    public TagLog merge(TagLog tagLog) {
        if (this.entityManager == null) this.entityManager = entityManager();
        TagLog merged = this.entityManager.merge(tagLog);
        this.entityManager.flush();
        return merged;
    }

    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

    @Transactional
    public void persist(TagLog tagLog) {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(tagLog);
    }

    @Transactional
    public void remove(Long id) {
        if (this.entityManager == null) this.entityManager = entityManager();
        TagLog attached = findTagLog(id);
        this.entityManager.remove(attached);
    }

    public List<String> findYears() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery("select cast(date_part('year', auth_date) as int) as year from tag_log group by year order by year desc");
        return q.getResultList();
    }

    public List<String> findApplications() {
        EntityManager em = entityManager;
        Query appNamesQuery = em.createNativeQuery("SELECT application_name FROM tag_log GROUP BY application_name ORDER BY COUNT(*) DESC;");
        return appNamesQuery.getResultList();
    }

    public TypedQuery<TagLog> findTagLogs(String searchString, String statusFilter, String applicationFilter, String sortFieldName, String sortOrder) {
        EntityManager em = entityManager;
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<TagLog> query = criteriaBuilder.createQuery(TagLog.class);
        Root<TagLog> c = query.from(TagLog.class);
        final List<Predicate> predicates = new ArrayList<Predicate>();
        final List<Order> orders = new ArrayList<Order>();

        if("DESC".equals(sortOrder.toUpperCase())){
            orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
        }else{
            orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
        }

        if(!StringUtils.isEmpty(applicationFilter)){
            predicates.add(criteriaBuilder.equal(c.get("applicationName"), applicationFilter));
        }
        if(!StringUtils.isEmpty(statusFilter)){
            predicates.add(criteriaBuilder.equal(c.get("status"), TagLog.Status.valueOf(statusFilter)));
        }

        if(!StringUtils.isEmpty(searchString)){
            Expression<Boolean> fullTestSearchExpression = criteriaBuilder.function("fts", Boolean.class, criteriaBuilder.literal("'"+searchString+"'"));
            Expression<Double> fullTestSearchRanking = criteriaBuilder.function("ts_rank", Double.class, criteriaBuilder.literal("'"+searchString+"'"));
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
            orders.add(criteriaBuilder.desc(fullTestSearchRanking));
        }

        orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);
        query.select(c);

        return em.createQuery(query);
    }


    public long countFindTagLogs(String searchString, String statusFilter, String applicationFilter) {
        EntityManager em = entityManager;
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<TagLog> c = query.from(TagLog.class);
        final List<Predicate> predicates = new ArrayList<Predicate>();

        if(!StringUtils.isEmpty(applicationFilter)){
            predicates.add(criteriaBuilder.equal(c.get("applicationName"), applicationFilter));
        }
        if(!StringUtils.isEmpty(statusFilter)){
            predicates.add(criteriaBuilder.equal(c.get("status"), TagLog.Status.valueOf(statusFilter)));
        }

        if(!StringUtils.isEmpty(searchString)){
            Expression<Boolean> fullTestSearchExpression = criteriaBuilder.function("fts", Boolean.class, criteriaBuilder.literal("'"+searchString+"'"));
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));

        query.select(criteriaBuilder.count(c));
        return em.createQuery(query).getSingleResult();
    }

}
