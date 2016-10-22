package org.jasr.sequelizer.dao;

import org.jasr.sequelizer.entities.SqlJob;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
@RepositoryRestResource
public interface SqlJobRepository extends CrudRepository<SqlJob, Long> {

}
