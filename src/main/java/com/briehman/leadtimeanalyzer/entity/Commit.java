package com.briehman.leadtimeanalyzer.entity;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "commits")
public class Commit extends AbstractCommit {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merge_hash", nullable = false)
    private Merge merge;

    protected Commit() {
    }

    public Commit(String hash, Instant authorDate, String branch, String ticket,
            Merge merge, User author, @NotNull CodeRepository repository) {
        super(hash, authorDate, branch, ticket, author, repository);
        this.merge = merge;
    }
}
