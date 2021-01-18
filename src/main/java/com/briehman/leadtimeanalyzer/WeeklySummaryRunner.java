package com.briehman.leadtimeanalyzer;

import com.briehman.leadtimeanalyzer.dto.DeliveryDateDto;
import com.briehman.leadtimeanalyzer.dto.PeriodDto;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.repository.CodeRepositoryRepository;
import com.briehman.leadtimeanalyzer.service.DeliveryDateStrategy;
import com.briehman.leadtimeanalyzer.service.MergeDataService;
import com.briehman.leadtimeanalyzer.service.MergeDateStrategy;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("weekly-summary")
public class WeeklySummaryRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklySummaryRunner.class);
    private static final int ROLLING_DAYS = 28;

    private final CodeRepositoryRepository codeRepositoryRepository;
    private final MergeDataService mergeDataService;

    @Autowired
    public WeeklySummaryRunner(CodeRepositoryRepository codeRepositoryRepository,
            MergeDataService mergeDataService) {
        this.codeRepositoryRepository = codeRepositoryRepository;
        this.mergeDataService = mergeDataService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        LOG.info("EXECUTING : command line runner");

        if (args.length == 0) {
            throw new IllegalArgumentException("Must provide repo name to populate");
        }

        String repoName = args[0];

        CodeRepository codeRepository = codeRepositoryRepository.findByName(repoName)
                .orElseThrow(() -> new RuntimeException(
                        "Unable to find repository with name '" + repoName + "'"));

        System.out.println(ROLLING_DAYS + " day rolling stats window ending on below dates");
        printStats(codeRepository, LocalDate.now());
        printStats(codeRepository, LocalDate.now().minus(2, ChronoUnit.WEEKS));
        printStats(codeRepository, LocalDate.now().minus(3, ChronoUnit.MONTHS));
        printStats(codeRepository, LocalDate.now().minus(6, ChronoUnit.MONTHS));
        printStats(codeRepository, LocalDate.now().minus(1, ChronoUnit.YEARS));
    }

    private void printStats(CodeRepository codeRepository, LocalDate endingOn) {
        DeliveryDateStrategy deliveryDateStrategy = new MergeDateStrategy();
        LocalDate twoWeeksAgo = endingOn.minus(ROLLING_DAYS, ChronoUnit.DAYS);

        PeriodDto leadTimeData = mergeDataService
                .getMergeData(twoWeeksAgo, endingOn, "America/Chicago", null, deliveryDateStrategy,
                        codeRepository, null, true, true, ROLLING_DAYS);
        List<DeliveryDateDto> data = leadTimeData.getData();
        DeliveryDateDto newestData = data.get(data.size() - 1);
        System.out.println(newestData.getDate() + ": " + newestData.getRollingStats().statusString());
    }
}
