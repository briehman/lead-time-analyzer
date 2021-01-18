package com.briehman.leadtimeanalyzer.repository;

import com.briehman.leadtimeanalyzer.entity.Commit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRepository extends CrudRepository<Commit, String> {

}
