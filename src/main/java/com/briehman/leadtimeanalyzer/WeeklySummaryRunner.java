package com.briehman.leadtimeanalyzer;

import com.briehman.leadtimeanalyzer.dto.DeliveryDateDto;
import com.briehman.leadtimeanalyzer.dto.PeriodDto;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.repository.CodeRepositoryRepository;
import com.briehman.leadtimeanalyzer.service.DeliveryDateStrategy;
import com.briehman.leadtimeanalyzer.service.MergeDataService;
import com.briehman.leadtimeanalyzer.service.MergeDateStrategy;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("weekly-summary")
public class WeeklySummaryRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklySummaryRunner.class);
    public static final String TIMEZONE = ZoneId.systemDefault().getId();
    public static final int DEFAULT_ROLLING_PERIOD = 28;

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
    public void run(ApplicationArguments args) {
        if (args.getNonOptionArgs().isEmpty()) {
            throw new IllegalArgumentException("Must provide repo name");
        }

        int rollingPeriod = args.containsOption("rollingPeriod")
                ? Integer.parseInt(args.getOptionValues("rollingPeriod").get(0))
                : DEFAULT_ROLLING_PERIOD;

        String repoName = args.getNonOptionArgs().get(0);

        CodeRepository codeRepository = codeRepositoryRepository.findByName(repoName)
                .orElseThrow(() -> new RuntimeException(
                        "Unable to find repository with name '" + repoName + "'"));

        System.out.println(rollingPeriod + " day rolling stats window ending on below dates. All values in days.");
        printStats(codeRepository, LocalDate.now(), rollingPeriod);
        printStats(codeRepository, LocalDate.now().minus(2, ChronoUnit.WEEKS), rollingPeriod);
        printStats(codeRepository, LocalDate.now().minus(3, ChronoUnit.MONTHS), rollingPeriod);
        printStats(codeRepository, LocalDate.now().minus(6, ChronoUnit.MONTHS), rollingPeriod);
        printStats(codeRepository, LocalDate.now().minus(1, ChronoUnit.YEARS), rollingPeriod);
    }

    private void printStats(CodeRepository codeRepository, LocalDate endingOn, int rollingPeriod) {
        DeliveryDateStrategy deliveryDateStrategy = new MergeDateStrategy();
        LocalDate twoWeeksAgo = endingOn.minus(rollingPeriod, ChronoUnit.DAYS);

        PeriodDto leadTimeData = mergeDataService
                .getMergeData(twoWeeksAgo, endingOn, TIMEZONE, null, deliveryDateStrategy,
                        codeRepository, null, true, true, rollingPeriod);
        List<DeliveryDateDto> data = leadTimeData.getData();
        DeliveryDateDto newestData = data.get(data.size() - 1);
        System.out.println(newestData.getDate() + ": " + newestData.getRollingStats().statusString());
    }
}
