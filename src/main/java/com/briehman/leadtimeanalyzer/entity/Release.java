package com.briehman.leadtimeanalyzer.entity;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "releases")
public class Release extends AbstractCommit {

    private String name;
    private Instant releaseDate;

    protected Release() {
    }

    public Release(String hash, Instant authorDate,
            User author, Instant releaseDate, String name, @NotNull CodeRepository codeRepository) {
        super(hash, authorDate, null, null, author, codeRepository);
        this.releaseDate = releaseDate;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

}
