package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.dto.ReleaseDto;
import com.briehman.leadtimeanalyzer.entity.Merge;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

public class ReleaseDateStrategy implements DeliveryDateStrategy {

    private final ZoneId timeZone;
    private final LocalDateTime nextLikelyRelease;

    public ReleaseDateStrategy(List<ReleaseDto> releases, ZoneId timeZone) {
        this.timeZone = timeZone;
        this.nextLikelyRelease = releases.stream()
                .map(ReleaseDto::getReleaseDate)
                .max(Comparator.naturalOrder())
                .map(d -> d.plusDays(14))
                .orElse(null);
    }

    @Override
    public Instant apply(Merge merge) {
        return merge.getRelease()
                .map(release -> release.getReleaseDate().atZone(timeZone).toInstant())
                .orElseGet(() -> {
                    ZonedDateTime zonedDateTime = nextLikelyRelease.atZone((timeZone));
                    return zonedDateTime.toInstant();
                });
    }
}
