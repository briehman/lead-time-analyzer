package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.dto.ReleaseDto;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.repository.ReleaseRepository;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReleaseService {

    private final ReleaseRepository releaseRepository;

    @Autowired
    public ReleaseService(ReleaseRepository releaseRepository) {
        this.releaseRepository = releaseRepository;
    }

    public List<ReleaseDto> getReleases(CodeRepository codeRepository, ZoneId zone) {
        return releaseRepository.findByRepository(codeRepository).stream()
                .map(release -> new ReleaseDto(release, zone))
                .sorted(Comparator.comparing(ReleaseDto::getReleaseDate))
                .collect(Collectors.toList());

    }
}
