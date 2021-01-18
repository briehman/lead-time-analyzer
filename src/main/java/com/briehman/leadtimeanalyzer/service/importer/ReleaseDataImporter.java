package com.briehman.leadtimeanalyzer.service.importer;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

import com.briehman.leadtimeanalyzer.DataLoaderRunner;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Release;
import com.briehman.leadtimeanalyzer.entity.User;
import com.briehman.leadtimeanalyzer.repository.ReleaseRepository;
import com.briehman.leadtimeanalyzer.repository.UserRepository;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Import releases for a code repository based off of the git tags for its
 * repository. The tag names must begin with the string "yyyy-MM-dd" to be parsed
 * properly.
 *
 * Releases are assumed to occur at 9 PM CT on the given date.
 */
@Service
public class ReleaseDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseDataImporter.class);

    private final UserRepository userRepository;
    private final ReleaseRepository releaseRepository;

    @Autowired
    private ReleaseDataImporter(UserRepository userRepository,
            ReleaseRepository releaseRepository) {
        this.userRepository = userRepository;
        this.releaseRepository = releaseRepository;
    }

    public void importReleases(CodeRepository codeRepository) throws IOException {
        List<Release> releases = getReleases(codeRepository);

        Optional<Release> latestRelease = releaseRepository
                .findFirstByRepositoryOrderByReleaseDateDesc(codeRepository);
        LOG.info("Last release loaded: " + latestRelease);

        List<Release> newReleases = latestRelease
                .map(latest -> releases.stream()
                        .filter(release -> latest.getReleaseDate()
                                .isBefore(release.getReleaseDate()))
                        .collect(Collectors.toList()))
                .orElse(releases);

        LOG.info("Saving {} new releases", newReleases.size());
        newReleases.forEach(releaseRepository::save);
    }

    /**
     * Get releases ordered by their release date. The date is computed using the tag name.
     */
    private List<Release> getReleases(CodeRepository codeRepository) throws IOException {
        Repository repo = codeRepository.getGitRepository();
        RevWalk revWalk = new RevWalk(repo);

        Pattern releaseTagPattern = Pattern
                .compile("refs/tags/(\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d).*");
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        return repo.getRefDatabase().getRefsByPrefix(R_TAGS)
                .stream()
                .map(tag -> {
                    Matcher matcher = releaseTagPattern.matcher(tag.getName());
                    boolean matches = matcher.matches();
                    if (matches) {
                        try {
                            Instant releaseDate = dateFormat.parse(matcher.group(1))
                                    .toInstant().atZone(ZoneId.of("America/Chicago"))
                                    .with(ChronoField.HOUR_OF_DAY, 21)
                                    .toInstant();

                            RevTag revTag = revWalk.parseTag(tag.getObjectId());

                            String releaseAuthorName = revTag.getTaggerIdent().getName();
                            User releaseAuthor = userRepository.findByUsername(releaseAuthorName)
                                    .orElseGet(
                                            () -> userRepository.save(new User(releaseAuthorName)));
                            return new Release(revTag.getName(),
                                    revTag.getTaggerIdent().getWhen().toInstant(),
                                    releaseAuthor, releaseDate, revTag.getShortMessage(),
                                    codeRepository);
                        } catch (Exception e) {
                            return null;
                        }
                    } else {
                        LOG.error("Tag " + tag.getName() + " does not match release format");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Release::getReleaseDate))
                .collect(Collectors.toList());
    }
}
