package com.briehman.leadtimeanalyzer.repository;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Release;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseRepository extends CrudRepository<Release, String> {

    @Override
    List<Release> findAll();

    List<Release> findByRepository(CodeRepository codeRepository);

    Optional<Release> findFirstByRepositoryOrderByReleaseDateDesc(CodeRepository codeRepository);
}
