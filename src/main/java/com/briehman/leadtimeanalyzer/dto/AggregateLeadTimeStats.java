package com.briehman.leadtimeanalyzer.dto;

import java.util.Collection;
import java.util.Optional;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class AggregateLeadTimeStats extends LeadTimeStats {

    private Double p25Minutes;
    private Double p50Minutes;
    private Double p75Minutes;
    private Double p90Minutes;
    private Double p99Minutes;

    public AggregateLeadTimeStats(Collection<LeadTimeDto> merges) {
        super(merges);
    }

    @Override
    protected void calculateStats(double[] oldestCommitPerMerge) {
        super.calculateStats(oldestCommitPerMerge);
        if (oldestCommitPerMerge.length > 0) {
            this.p25Minutes = new Percentile(25d).evaluate(oldestCommitPerMerge);
            this.p50Minutes = new Percentile(50d).evaluate(oldestCommitPerMerge);
            this.p75Minutes = new Percentile(75d).evaluate(oldestCommitPerMerge);
            this.p90Minutes = new Percentile(90d).evaluate(oldestCommitPerMerge);
            this.p99Minutes = new Percentile(99d).evaluate(oldestCommitPerMerge);
        }
    }

    public Double getP25Minutes() {
        return p25Minutes;
    }

    public Double getP50Minutes() {
        return p50Minutes;
    }

    public Double getP75Minutes() {
        return p75Minutes;
    }

    public Double getP90Minutes() {
        return p90Minutes;
    }

    public Double getP99Minutes() {
        return p99Minutes;
    }

    @Override
    public String toString() {
        return "AggregateLeadTimeStats{" +
                "p25Minutes=" + p25Minutes +
                ", p50Minutes=" + p50Minutes +
                ", p75Minutes=" + p75Minutes +
                ", p90Minutes=" + p90Minutes +
                ", p99Minutes=" + p99Minutes +
                '}';
    }

    public String statusString() {
        return "p25Days=" + minutesToDays(p25Minutes) +
                ", p50Days=" + minutesToDays(p50Minutes) +
                ", p75Days=" + minutesToDays(p75Minutes) +
                ", p90Days=" + minutesToDays(p90Minutes) +
                ", p99Days=" + minutesToDays(p99Minutes);
    }

    private static String minutesToDays(Double mins) {
        return Optional.ofNullable(mins)
                .map(d -> String.format("%.2f", d / 60 / 24))
                .orElse(null);
    }
}
