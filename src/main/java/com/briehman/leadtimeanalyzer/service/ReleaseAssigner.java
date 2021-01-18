package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Merge;
import com.briehman.leadtimeanalyzer.entity.Release;
import com.briehman.leadtimeanalyzer.repository.MergeRepository;
import com.briehman.leadtimeanalyzer.repository.ReleaseRepository;
import com.briehman.leadtimeanalyzer.service.importer.MergeDataImporter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Assigns a release date to any merges which do not have a release date and a
 * release occurs after the merge. The first release after that merge is assumed
 * to contain that merge.
 */
@Service
public class ReleaseAssigner {

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseAssigner.class);

    private final ReleaseRepository releaseRepository;
    private final MergeRepository mergeRepository;

    @Autowired
    public ReleaseAssigner(ReleaseRepository releaseRepository, MergeRepository mergeRepository) {
        this.releaseRepository = releaseRepository;
        this.mergeRepository = mergeRepository;
    }

    public void assignRelease(CodeRepository codeRepository) {
        List<Release> releases = releaseRepository.findByRepository(codeRepository);
        ReleaseCalculator calculator = new ReleaseCalculator(releases);
        for (Merge merge : mergeRepository.findAllByRepositoryAndReleaseIsNull(codeRepository)) {
            calculator.apply(merge).ifPresent(release -> {
                LOG.debug("Merge " + merge.getHash() + " has release " + release.getName());
                merge.setRelease(release);
                mergeRepository.save(merge);
            });
        }
    }

    private static class ReleaseCalculator implements Function<Merge, Optional<Release>> {

        private final List<Release> releases;

        public ReleaseCalculator(List<Release> releases) {
            this.releases = releases.stream()
                    .sorted(Comparator.comparing(Release::getReleaseDate))
                    .collect(Collectors.toList());
        }

        public Optional<Release> apply(Merge merge) {
            return releases.stream()
                    .filter(release -> release.getAuthorDate().isAfter(merge.getAuthorDate()))
                    .findFirst();
        }

    }

}
