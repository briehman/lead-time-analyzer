package com.briehman.leadtimeanalyzer.service.importer;

import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Commit;
import com.briehman.leadtimeanalyzer.entity.Merge;
import com.briehman.leadtimeanalyzer.entity.User;
import com.briehman.leadtimeanalyzer.repository.CommitRepository;
import com.briehman.leadtimeanalyzer.repository.MergeRepository;
import com.briehman.leadtimeanalyzer.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Import the merges for the code repository and associated ticket information.
 */
@Service
public class MergeDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(MergeDataImporter.class);

    private final MergeRepository mergeRepository;
    private final UserRepository userRepository;
    private final CommitRepository commitRepository;

    private final Pattern mergePattern = Pattern.compile("Merge branch '((.*?-.*?)-.*?)' into.*");
    private final Pattern commitShortMessageTicketPattern = Pattern.compile("^(.*?-\\d+?)\\s.*");
    private final Pattern commitFullMessageTicketPattern = Pattern.compile("(m)^(.*?-\\d+?)\\s.*");

    @Autowired
    private MergeDataImporter(MergeRepository mergeRepository, UserRepository userRepository,
            CommitRepository commitRepository) {
        this.mergeRepository = mergeRepository;
        this.userRepository = userRepository;
        this.commitRepository = commitRepository;
    }

    public void importMerges(CodeRepository codeRepository, LocalDate since) throws IOException, GitAPIException {
        String branch = codeRepository.getPrimaryBranch();
        Repository repo = codeRepository.getGitRepository();
        RevCommit devCommit = getBranchCommit(branch, repo);
        Date after = Date.from(since.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        RevWalk revWalk = new RevWalk(repo);
        revWalk.markStart(devCommit);

        Optional<Merge> latestMerge = mergeRepository
                .findFirstByRepositoryOrderByAuthorDateDesc(codeRepository);

        LOG.info("Last merge loaded: " + latestMerge);

        List<RevFilter> mergeFilters = Arrays.asList(
                RevFilter.ONLY_MERGES,
                CommitTimeRevFilter.after(after),
                MessageRevFilter.create(String.format("into '%s'", branch)),
                MessageRevFilter.create("Revert \"").negate());
        revWalk.setRevFilter(AndRevFilter.create(mergeFilters));

        Iterator<RevCommit> iterator = revWalk.iterator();

        int merges = 0;
        int commits = 0;

        while (iterator.hasNext()) {
            RevCommit merge = iterator.next();

            if (latestMerge.isPresent()
                    && merge.getName().equals(latestMerge.get().getHash())) {
                LOG.debug("Stopping at point of last loaded merge - "
                        + merge.getName());
                break;
            }
            merges++;

            RevCommit[] parents = merge.getParents();

            String parentsJoined = Stream.of(parents)
                    .map(RevCommit::getName)
                    .collect(Collectors.joining(", "));

            if (parents.length < 2) {
                throw new IllegalArgumentException(String.format(
                        "Attempting to walk commit parents for commit %s but has fewer "
                                + "than two parents: %s", merge.getName(), parentsJoined));
            } else {
                Matcher mergeMatcher = mergePattern.matcher(merge.getShortMessage());

                if (mergeMatcher.matches()) {
                    String mergedBranch = mergeMatcher.group(1);
                    String ticket = mergeMatcher.group(2);
                    Merge savedMerge = saveMerge(merge, parentsJoined, codeRepository,
                            mergedBranch, ticket);
                    for (RevCommit commit : getMergeCommits(parents, codeRepository)) {
                        Optional<String> commitTicket = getTicket(commit);
                        saveCommit(savedMerge, commit, codeRepository, commitTicket.orElse(null));
                        commits++;
                    }
                } else {
                    LOG.debug("Skipping merge {} whch does not match message format - '{}'",
                            merge.getName(), merge.getShortMessage());
                }
            }
        }

        LOG.info("{} merges containing {} commits", merges, commits);
    }

    private Optional<String> getTicket(RevCommit commit) {
        Matcher shortMatcher = commitShortMessageTicketPattern.matcher(commit.getShortMessage());
        if (shortMatcher.matches()) {
            return Optional.of(shortMatcher.group(1));
        } else {
            Matcher fullMatcher = commitFullMessageTicketPattern.matcher(commit.getFullMessage());
            if (fullMatcher.matches()) {
                return Optional.of(fullMatcher.group(1));
            }
        }

        return Optional.empty();
    }

    private Iterable<RevCommit> getMergeCommits(RevCommit[] parents,
            CodeRepository codeRepository)
            throws IOException, GitAPIException {
        LogCommand logCommand = new Git(codeRepository.getGitRepository()).log()
                .not(parents[0]);
        for (int i = 1; i < parents.length; i++) {
            logCommand.add(parents[i]);
        }
        logCommand.setRevFilter(RevFilter.NO_MERGES);

        return logCommand.call();
    }

    private Merge saveMerge(RevCommit merge, String parentsJoined,
            @NotNull CodeRepository codeRepository, String mergedBranch, String ticket) {
        String mergeAuthorName = merge.getAuthorIdent().getName();

        User mergeAuthor = userRepository.findByUsername(mergeAuthorName)
                .orElseGet(() -> userRepository.save(new User(mergeAuthorName)));

        return mergeRepository
                .save(new Merge(merge.getName(), parentsJoined,
                        merge.getAuthorIdent().getWhen().toInstant(), mergedBranch, ticket,
                        mergeAuthor, codeRepository));
    }

    private void saveCommit(Merge savedMerge, RevCommit commit,
            @NotNull CodeRepository codeRepository, @Nullable String ticket) {
        String commitAuthorName = commit.getAuthorIdent().getName();

        User commitAuthor = userRepository.findByUsername(commitAuthorName)
                .orElseGet(() -> userRepository.save(new User(commitAuthorName)));

        String commitHash = commit.getName();

        if (commitRepository.findById(commitHash).isEmpty()) {
            commitRepository.save(new Commit(commitHash,
                    commit.getAuthorIdent().getWhen().toInstant(), savedMerge.getBranch(),
                    ticket, savedMerge, commitAuthor, codeRepository));
        }
    }


    private RevCommit getBranchCommit(String branch, Repository repo) throws IOException {
        RevCommit devCommit;
        try (RevWalk revWalk = new RevWalk(repo)) {
            devCommit = revWalk.parseCommit(repo.resolve(branch));
        }
        return devCommit;
    }
}
