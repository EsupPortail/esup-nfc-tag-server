package org.esupportail.nfctag.repositories;

import org.esupportail.nfctag.domain.Application;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ApplicationRepository extends PagingAndSortingRepository<Application, Long>, CrudRepository<Application, Long> {
}
