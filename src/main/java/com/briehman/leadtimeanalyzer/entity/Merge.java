package com.briehman.leadtimeanalyzer.entity;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.eclipse.jgit.revwalk.RevCommit;

@Entity
@Table(name = "merges")
public class Merge extends AbstractCommit {

    @NotNull
    private String parents;

    @OneToMany(cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        mappedBy = "merge")
    private final Set<Commit> commits = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_hash")
    private Release release;

    protected Merge() {
    }

    public Merge(String hash, String parents, Instant authorDate, String branch, String ticket,
            User author, @NotNull CodeRepository codeRepository) {
        super(hash, authorDate, branch, ticket, author, codeRepository);
        this.parents = parents;
    }

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public Set<Commit> getCommits() {
        return commits;
    }

    public Optional<Team> getTeam() {
        return Optional.ofNullable(team);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Optional<Release> getRelease() {
        return Optional.ofNullable(release);
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    @Override
    public String toString() {
        return "Merge{" +
                "hash='" + hash + '\'' +
                ", parents='" + parents + '\'' +
                ", authorDate=" + authorDate +
                ", branch='" + branch + '\'' +
                ", ticket='" + ticket + '\'' +
                '}';
    }
}
