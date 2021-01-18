package com.briehman.leadtimeanalyzer.entity;

import java.time.Instant;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@MappedSuperclass
public abstract class AbstractCommit {

    @Id
    protected String hash;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    protected CodeRepository repository;

    @NotNull
    protected Instant authorDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    protected User author;

    @Size(max = 64)
    protected String branch;

    @Size(max = 64)
    protected String ticket;

    protected AbstractCommit() {
    }

    public AbstractCommit(String hash, Instant authorDate, String branch, String ticket,
            User author, CodeRepository repository) {
        this.repository = repository;
        this.hash = hash;
        this.authorDate = authorDate;
        this.branch = branch;
        this.ticket = ticket;
        this.author = author;
    }

    public CodeRepository getRepository() {
        return repository;
    }

    public void setRepository(CodeRepository repository) {
        this.repository = repository;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Instant getAuthorDate() {
        return authorDate;
    }

    public void setAuthorDate(Instant authorDate) {
        this.authorDate = authorDate;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
