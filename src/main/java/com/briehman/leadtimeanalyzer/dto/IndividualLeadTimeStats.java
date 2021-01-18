package com.briehman.leadtimeanalyzer.dto;

import java.util.Collection;
import java.util.stream.Collectors;

public class IndividualLeadTimeStats extends LeadTimeStats {

    private Collection<String> authors;

    public IndividualLeadTimeStats(LeadTimeDto merge) {
        super(merge);
        this.authors = merge.getCommits().stream()
                .map(CommitDto::getAuthorName)
                .distinct()
                .collect(Collectors.toList());
    }

    public Collection<String> getAuthors() {
        return authors;
    }
}
