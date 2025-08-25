package org.esupportail.nfctag.repositories;

import org.esupportail.nfctag.domain.TagLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TagLogRepository extends JpaRepository<TagLog, Long>, JpaSpecificationExecutor<TagLog> {

}
