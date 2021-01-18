package com.briehman.leadtimeanalyzer.dto;

import com.briehman.leadtimeanalyzer.entity.Release;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ReleaseDto {

    private final String name;
    private final LocalDateTime releaseDate;
    private final LocalDateTime authorDate;
    private final String user;

    public ReleaseDto(Release release, ZoneId zone) {
        this.name = release.getName();
        this.releaseDate = release.getReleaseDate().atZone(zone).toLocalDateTime();
        this.authorDate = release.getAuthorDate().atZone(zone).toLocalDateTime();
        this.user = release.getAuthor().getUsername();
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public LocalDateTime getAuthorDate() {
        return authorDate;
    }

    public String getUser() {
        return user;
    }
}
