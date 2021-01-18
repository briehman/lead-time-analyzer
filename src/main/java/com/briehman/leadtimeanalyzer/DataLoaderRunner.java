package com.briehman.leadtimeanalyzer;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.repository.CodeRepositoryRepository;
import com.briehman.leadtimeanalyzer.service.importer.RepositoryDataImporter;
import java.time.LocalDate;
import java.time.Month;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("data-loader")
public class DataLoaderRunner implements CommandLineRunner {

    public static final LocalDate MERGES_SINCE_DEFAULT = LocalDate.of(2019, Month.JANUARY, 1);
    private final CodeRepositoryRepository codeRepositoryRepository;
    private final RepositoryDataImporter repositoryDataImporter;

    private static final Logger LOG = LoggerFactory.getLogger(DataLoaderApplication.class);

    @Autowired
    public DataLoaderRunner(CodeRepositoryRepository codeRepositoryRepository,
            RepositoryDataImporter repositoryDataImporter) {
        this.codeRepositoryRepository = codeRepositoryRepository;
        this.repositoryDataImporter = repositoryDataImporter;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("EXECUTING : command line runner");

        if (args.length == 0) {
            throw new IllegalArgumentException("Must provide repo name to populate");
        }

        String repoName = args[0];
        LocalDate mergesSince;
        if (args.length > 1) {
            mergesSince = LocalDate.parse(args[1]);
        } else {
            mergesSince = MERGES_SINCE_DEFAULT;
        }

        CodeRepository codeRepository = codeRepositoryRepository.findByName(repoName)
                .orElseThrow(() -> new RuntimeException(
                        "Unable to find repository with name '" + repoName + "'"));

        repositoryDataImporter.importData(codeRepository, mergesSince);
    }
}
