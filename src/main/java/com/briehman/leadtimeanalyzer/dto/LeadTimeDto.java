package com.briehman.leadtimeanalyzer.dto;

import com.briehman.leadtimeanalyzer.entity.Merge;
import com.briehman.leadtimeanalyzer.entity.Team;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeadTimeDto {

    private final String hash;
    private final String ticket;
    private final String team;
    private final Instant deliveryDate;
    private final List<CommitDto> commits;

    public LeadTimeDto(Merge merge, Instant deliveryDate) {
        this.hash = merge.getHash();
        this.ticket = merge.getTicket();
        this.deliveryDate = deliveryDate;
        this.commits = merge.getCommits().stream()
                .map(CommitDto::new)
                .collect(Collectors.toList());
        this.team = merge.getTeam().map(Team::getName).orElse(null);
    }

    public String getHash() {
        return hash;
    }

    public String getTicket() {
        return ticket;
    }

    public String getTeam() {
        return team;
    }

    public Instant getDeliveryDate() {
        return deliveryDate;
    }

    List<CommitDto> getCommits() {
        return commits;
    }

    Optional<Long> getMaxMinutes() {
        return commits.stream()
                .map(commit -> ChronoUnit.MINUTES.between(commit.getAuthorDate(), deliveryDate))
                .filter(minutes -> minutes != 0L)
                .max(Long::compareTo);
    }

    public LeadTimeStats getStats() {
        return new IndividualLeadTimeStats(this);
    }
}
