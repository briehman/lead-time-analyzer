package com.briehman.leadtimeanalyzer.repository;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Merge;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MergeRepository extends CrudRepository<Merge, String> {

    Optional<Merge> findFirstByRepositoryOrderByAuthorDateDesc(CodeRepository codeRepository);

    List<Merge> findAllByRepositoryAndAuthorDateGreaterThanEqualAndAuthorDateLessThanEqual(CodeRepository codeRepository, Instant start, Instant end);

    List<Merge> findAllByRepositoryAndReleaseIsNull(CodeRepository codeRepository);

    List<Merge> findAllByRepositoryAndTeamIsNull(CodeRepository codeRepository);
}
