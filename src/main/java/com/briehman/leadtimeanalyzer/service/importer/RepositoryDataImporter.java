package com.briehman.leadtimeanalyzer.service.importer;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.service.ReleaseAssigner;
import com.briehman.leadtimeanalyzer.service.TeamAssigner;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class RepositoryDataImporter {

    private final ReleaseDataImporter releaseDataImporter;
    private final MergeDataImporter mergeDataImporter;
    private final TeamAssigner teamAssigner;
    private final ReleaseAssigner releaseAssigner;

    private RepositoryDataImporter(ReleaseDataImporter releaseDataImporter,
            MergeDataImporter mergeDataImporter, TeamAssigner teamAssigner,
            ReleaseAssigner releaseAssigner) {
        this.releaseDataImporter = releaseDataImporter;
        this.mergeDataImporter = mergeDataImporter;
        this.teamAssigner = teamAssigner;
        this.releaseAssigner = releaseAssigner;
    }

    public void importData(CodeRepository codeRepository, LocalDate mergesSince) throws Exception {
        releaseDataImporter.importReleases(codeRepository);
        mergeDataImporter.importMerges(codeRepository, mergesSince);
        releaseAssigner.assignRelease(codeRepository);
        if (codeRepository.getUsesTeams()) {
            //teamAssigner.assignTeams(codeRepository);
        }
    }
}
