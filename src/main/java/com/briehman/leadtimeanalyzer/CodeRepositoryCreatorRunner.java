package com.briehman.leadtimeanalyzer;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.repository.CodeRepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("code-repository")
public class CodeRepositoryCreatorRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CodeRepositoryCreatorRunner.class);

    private final CodeRepositoryRepository codeRepositoryRepository;

    @Autowired
    public CodeRepositoryCreatorRunner(CodeRepositoryRepository codeRepositoryRepository) {
        this.codeRepositoryRepository = codeRepositoryRepository;
    }

    @Override
    public void run(String... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Must provide repo name, directory, and primary branch as arguments");
        }

        String repoName = args[0];
        String filePath = args[1];
        String primaryBranch = args[2];
        boolean usesTeams = args.length > 3 && Boolean.parseBoolean(args[3]);

        CodeRepository repo = codeRepositoryRepository.save(new CodeRepository(repoName, filePath,
                primaryBranch, usesTeams));
        LOG.info("Created {} repository at path {} with ID {}",
                repo.getName(), repo.getFilePath(), repo.getId());
    }
}
