package com.briehman.leadtimeanalyzer.repository;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepositoryRepository extends CrudRepository<CodeRepository, Long> {

    Optional<CodeRepository> findByName(String name);
}
