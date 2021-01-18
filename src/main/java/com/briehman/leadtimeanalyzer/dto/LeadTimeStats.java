package com.briehman.leadtimeanalyzer.dto;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;

public abstract class LeadTimeStats {

    private final Long minMinutes;
    private final Long maxMinutes;
    private final int mergeCount;
    private final int commitCount;
    private Double meanMinutes;
    private Double medianMinutes;
    private Double standardDeviationMinutes;

    public LeadTimeStats(Collection<LeadTimeDto> merges) {
        this.maxMinutes = merges.stream()
                .map(LeadTimeDto::getMaxMinutes)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Long::compareTo).orElse(null);
        this.minMinutes = merges.stream()
                .map(LeadTimeDto::getMaxMinutes)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Long::compareTo).orElse(null);
        this.commitCount = merges.stream()
                .map(merge -> merge.getCommits().size())
                .reduce(0, Integer::sum);
        this.mergeCount = merges.size();

        double[] oldestCommitPerMerge = merges.stream()
                .map(LeadTimeDto::getMaxMinutes)
                .filter(Optional::isPresent)
                .mapToDouble(maxMinsToMerge -> maxMinsToMerge.get().doubleValue())
                .sorted()
                .toArray();

        calculateStats(oldestCommitPerMerge);
    }

    public LeadTimeStats(LeadTimeDto merge) {
        this.maxMinutes = merge.getCommits().stream()
                .map(commit -> ChronoUnit.MINUTES.between(commit.getAuthorDate(), merge.getDeliveryDate()))
                .max(Long::compareTo).orElse(null);
        this.minMinutes = merge.getCommits().stream()
                .map(commit -> ChronoUnit.MINUTES.between(commit.getAuthorDate(), merge.getDeliveryDate()))
                .min(Long::compareTo).orElse(null);
        this.commitCount = merge.getCommits().size();
        this.mergeCount = 1;

        double[] minutesToMergePerCommit = merge.getCommits().stream()
                .map(commit -> ChronoUnit.MINUTES.between(commit.getAuthorDate(), merge.getDeliveryDate()))
                .mapToDouble(Long::doubleValue)
                .sorted()
                .toArray();

        calculateStats(minutesToMergePerCommit);
    }

    protected void calculateStats(double[] oldestCommitPerMerge) {
        if (oldestCommitPerMerge.length > 0) {
            this.standardDeviationMinutes = new StandardDeviation().evaluate(oldestCommitPerMerge);
            this.meanMinutes = new Mean().evaluate(oldestCommitPerMerge);
            this.medianMinutes = new Median().evaluate(oldestCommitPerMerge);
        }
    }

    public Long getMinMinutes() {
        return minMinutes;
    }

    public Long getMaxMinutes() {
        return maxMinutes;
    }

    public int getMergeCount() {
        return mergeCount;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public Double getStandardDeviationMinutes() {
        return standardDeviationMinutes;
    }

    public Double getMeanMinutes() {
        return meanMinutes;
    }

    public Double getMedianMinutes() {
        return medianMinutes;
    }
}
