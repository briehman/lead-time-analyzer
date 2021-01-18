package com.briehman.leadtimeanalyzer.dto;

import com.briehman.leadtimeanalyzer.entity.Commit;
import java.time.Instant;

public class CommitDto {
    private final String hash;
    private final Instant authorDate;
    private final String authorName;

    public CommitDto(Commit commit) {
        this.hash = commit.getHash();
        this.authorDate = commit.getAuthorDate();
        this.authorName = commit.getAuthor().getUsername();
    }

    public String getHash() {
        return hash;
    }

    public Instant getAuthorDate() {
        return authorDate;
    }

    public String getAuthorName() {
        return authorName;
    }
}
