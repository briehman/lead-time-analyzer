package com.briehman.leadtimeanalyzer.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PeriodDto {
    private final LocalDate start;
    private final LocalDate stop;
    private final List<DeliveryDateDto> data;
    private final AggregateLeadTimeStats stats;

    public PeriodDto(List<DeliveryDateDto> data, LocalDate start, LocalDate stop) {
        this.data = data;
        this.start = start;
        this.stop = stop;
        List<LeadTimeDto> allMerges = data.stream()
                .flatMap(dto -> dto.getLeadTimes().stream())
                .collect(Collectors.toList());
        this.stats = new AggregateLeadTimeStats(allMerges);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getStop() {
        return stop;
    }

    public List<DeliveryDateDto> getData() {
        return data;
    }

    public AggregateLeadTimeStats getStats() {
        return stats;
    }
}
