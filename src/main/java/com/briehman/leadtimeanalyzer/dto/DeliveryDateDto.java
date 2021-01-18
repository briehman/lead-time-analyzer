package com.briehman.leadtimeanalyzer.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DeliveryDateDto {
    private final LocalDate date;
    private final List<LeadTimeDto> leadTimes;
    private final AggregateLeadTimeStats leadTimeStats;
    private final List<LeadTimeDto> rollingLeadTimes;
    private final AggregateLeadTimeStats rollingLeadTimeStats;

    private DeliveryDateDto(LocalDate date, List<LeadTimeDto> leadTimes, List<LeadTimeDto> rollingLeadTimes) {
        this(date, leadTimes, new AggregateLeadTimeStats(leadTimes), rollingLeadTimes,
                new AggregateLeadTimeStats(rollingLeadTimes));
    }

    private DeliveryDateDto(LocalDate date,
            List<LeadTimeDto> leadTimes, AggregateLeadTimeStats leadTimeStats,
            List<LeadTimeDto> rollingLeadTimes, AggregateLeadTimeStats rollingLeadTimeStats) {
        this.date = date;
        this.leadTimes = leadTimes;
        this.leadTimeStats = leadTimeStats;
        this.rollingLeadTimes = rollingLeadTimes;
        this.rollingLeadTimeStats = rollingLeadTimeStats;
    }

    public static DeliveryDateDto withDetails(LocalDate currentDate,
            List<LeadTimeDto> dateLeadTimes, List<LeadTimeDto> rollingLeadTimes) {
        return new DeliveryDateDto(currentDate,
                dateLeadTimes, new AggregateLeadTimeStats(dateLeadTimes),
                rollingLeadTimes, new AggregateLeadTimeStats(rollingLeadTimes));
    }

    public static DeliveryDateDto withoutDetails(LocalDate currentDate,
            List<LeadTimeDto> dateLeadTimes, List<LeadTimeDto> rollingLeadTimes) {
        return new DeliveryDateDto(currentDate,
                Collections.emptyList(), new AggregateLeadTimeStats(dateLeadTimes),
                Collections.emptyList(), new AggregateLeadTimeStats(rollingLeadTimes));
    }

    public static DeliveryDateDto empty(LocalDate currentDate) {
        return new DeliveryDateDto(currentDate, Collections.emptyList(), Collections.emptyList());
    }

    public static DeliveryDateDto empty(LocalDate currentDate, List<LeadTimeDto> rollingLeadTimes) {
        return new DeliveryDateDto(currentDate, Collections.emptyList(), rollingLeadTimes);
    }

    public LocalDate getDate() {
        return date;
    }

    public AggregateLeadTimeStats getStats() {
        return leadTimeStats;
    }

    public AggregateLeadTimeStats getRollingStats() {
        return rollingLeadTimeStats;
    }

    public List<LeadTimeDto> getLeadTimes() {
        return leadTimes;
    }

    public List<LeadTimeDto> getRollingLeadTimes() {
        return rollingLeadTimes;
    }
}
