package com.briehman.leadtimeanalyzer.controller;

import com.briehman.leadtimeanalyzer.dto.PeriodDto;
import com.briehman.leadtimeanalyzer.dto.ReleaseDto;
import com.briehman.leadtimeanalyzer.dto.UserDto;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.repository.CodeRepositoryRepository;
import com.briehman.leadtimeanalyzer.service.DeliveryDateStrategy;
import com.briehman.leadtimeanalyzer.service.MergeDataService;
import com.briehman.leadtimeanalyzer.service.MergeDateStrategy;
import com.briehman.leadtimeanalyzer.service.ReleaseDateStrategy;
import com.briehman.leadtimeanalyzer.service.ReleaseService;
import com.briehman.leadtimeanalyzer.service.UserService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppRestController {

    private static final String LOCALHOST = "http://localhost:3000";
    private final MergeDataService mergeDataService;
    private final UserService userService;
    private final ReleaseService releaseService;
    private final CodeRepositoryRepository codeRepositoryRepository;

    @Autowired
    public AppRestController(MergeDataService mergeDataService,
            UserService userService, ReleaseService releaseService,
            CodeRepositoryRepository codeRepositoryRepository) {
        this.mergeDataService = mergeDataService;
        this.userService = userService;
        this.releaseService = releaseService;
        this.codeRepositoryRepository = codeRepositoryRepository;
    }

    @GetMapping("/leadTime")
    @CrossOrigin(origins = LOCALHOST)
    public PeriodDto getLeadTime(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate stop,
            @RequestParam(defaultValue = "UTC") String timezoneName,
            @RequestParam(name = "author", required = false) List<String> authorNames,
            @RequestParam(name = "team", required = false) List<String> teamNames,
            @RequestParam(name = "deliveryDateStrategy", defaultValue = "mergeDate") String deliveryDateStrategyName,
            @RequestParam(name = "repoName") String repoName,
            @RequestParam(name = "fill", defaultValue = "true") boolean fillEmptyDates,
            @RequestParam(name = "details", defaultValue = "true") boolean withDetails,
            @RequestParam(name = "rolling", defaultValue = "14") int rollingPeriod) {

        CodeRepository codeRepository = codeRepositoryRepository.findByName(repoName)
                .orElseThrow(() -> new RuntimeException("Repo '" + repoName + "' not found"));
        DeliveryDateStrategy deliveryDateStrategy = getDeliveryDateStrategy(timezoneName,
                deliveryDateStrategyName, codeRepository);
        return mergeDataService
                .getMergeData(start, stop, timezoneName, authorNames, deliveryDateStrategy,
                        codeRepository, teamNames, fillEmptyDates, withDetails, rollingPeriod);
    }

    private DeliveryDateStrategy getDeliveryDateStrategy(String timezoneName,
            String deliveryDateStrategyName, CodeRepository codeRepository) {
        DeliveryDateStrategy deliveryDateStrategy;
        if ("mergeDate".equals(deliveryDateStrategyName)) {
            deliveryDateStrategy = new MergeDateStrategy();
        } else if ("releaseDate".equals(deliveryDateStrategyName)) {
            ZoneId zone = ZoneId.of(timezoneName);
            List<ReleaseDto> releases = releaseService.getReleases(codeRepository, zone);
            deliveryDateStrategy = new ReleaseDateStrategy(releases, zone);
        } else {
            throw new IllegalArgumentException("Unsupported release date strategy - " + deliveryDateStrategyName);
        }
        return deliveryDateStrategy;
    }

    @GetMapping("/users")
    @CrossOrigin(origins = LOCALHOST)
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/releases")
    @CrossOrigin(origins = LOCALHOST)
    public List<ReleaseDto> getRelases(@RequestParam(defaultValue = "UTC") String timezoneName,
            @RequestParam(name = "repoName") String repoName) {
        CodeRepository codeRepository = codeRepositoryRepository.findByName(repoName)
                .orElseThrow(() -> new RuntimeException("Repo not found"));
        ZoneId zone = ZoneId.of(timezoneName);
        return releaseService.getReleases(codeRepository, zone);
    }
}
