package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.dto.DeliveryDateDto;
import com.briehman.leadtimeanalyzer.dto.LeadTimeDto;
import com.briehman.leadtimeanalyzer.dto.PeriodDto;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Commit;
import com.briehman.leadtimeanalyzer.entity.Merge;
import com.briehman.leadtimeanalyzer.repository.MergeRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MergeDataService {

    private final MergeRepository mergeRepository;

    @Autowired
    public MergeDataService(MergeRepository mergeRepository) {
        this.mergeRepository = mergeRepository;
    }

    static class NotSquashMergeFilter implements Predicate<Merge> {

        private static final int MIN_MINUTES = 5;

        @Override
        public boolean test(Merge merge) {
            Set<Commit> commits = merge.getCommits();
            boolean isSquash;
            if (commits.size() == 1) {
                long minutesBetweenMergeAndCommit = Math.abs(ChronoUnit.MINUTES.between(
                        merge.getAuthorDate(),
                        commits.iterator().next().getAuthorDate()));
                isSquash = MIN_MINUTES > minutesBetweenMergeAndCommit;
            } else {
                isSquash = false;
            }

            return !isSquash;
        }
    }

    static class NotQaTicketFilter implements Predicate<Merge> {
        @Override
        public boolean test(Merge merge) {
            return !merge.getTicket().startsWith("QA");
        }
    }

    static class NotTermTicketFilter implements Predicate<Merge> {
        @Override
        public boolean test(Merge merge) {
            return !merge.getTicket().startsWith("TERM");
        }
    }

    public PeriodDto getMergeData(LocalDate start, LocalDate stop, String timeZoneName,
            Collection<String> authorNames, DeliveryDateStrategy deliveryDateStrategy,
            CodeRepository codeRepository, Collection<String> teamNames, boolean fillEmptyDates,
            boolean withDetails, int rolling) {
        ZoneId timeZone = ZoneId.of(timeZoneName);

        List<Merge> merges = mergeRepository
                .findAllByRepositoryAndAuthorDateGreaterThanEqualAndAuthorDateLessThanEqual(
                        codeRepository,
                        start.atStartOfDay(timeZone).toInstant(),
                        stop.plusDays(1).atStartOfDay(timeZone).toInstant());

        Predicate<Merge> authorFilter = Optional.ofNullable(authorNames)
                .map(merge -> (Predicate<Merge>) merge1 -> merge1.getCommits().stream()
                .anyMatch(commit -> authorNames.contains(commit.getAuthor().getUsername())))
                .orElse(merge -> true);

        Predicate<Merge> teamFilter = Optional.ofNullable(teamNames)
                .map(names -> (Predicate<Merge>) m -> m.getTeam()
                        .map(team -> names.contains(team.getName()))
                        .orElse(false))
                .orElse(merge -> true);

        Map<LocalDate, List<Merge>> mergesByDate = merges.stream()
                .collect(Collectors
                        .groupingBy(merge -> merge.getAuthorDate().atZone(timeZone).toLocalDate()));

        Map<LocalDate, List<LeadTimeDto>> leadTimesByDate = mergesByDate
                .entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stream()
                        .filter(new NotQaTicketFilter())
                        .filter(new NotSquashMergeFilter())
                        .filter(authorFilter)
                        .filter(teamFilter)
                        .map(merge -> Optional
                                .ofNullable(deliveryDateStrategy.apply(merge))
                                .map(deliveryDate -> new LeadTimeDto(merge,
                                        deliveryDate))
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                )).entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a));

        Map<LocalDate, DeliveryDateDto> known = leadTimesByDate.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> {
                            LocalDate date = e.getKey();
                            List<LeadTimeDto> dayLeadTimes = e.getValue();
                            List<LeadTimeDto> rollingLeadTimes = getRollingLeadTimes(
                                    rolling, leadTimesByDate, date);

                            if (withDetails) {
                                return DeliveryDateDto.withDetails(e.getKey(), dayLeadTimes,
                                        rollingLeadTimes);
                            } else {
                                return DeliveryDateDto.withoutDetails(e.getKey(), dayLeadTimes,
                                        rollingLeadTimes);
                            }

                        }
                ));

        if (fillEmptyDates) {
            LocalDate currentDate = start;
            while (!currentDate.isAfter(stop)) {
                if (!known.containsKey(currentDate)) {
                    known.put(currentDate, DeliveryDateDto.empty(currentDate,
                            getRollingLeadTimes(rolling, leadTimesByDate, currentDate)));
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        List<DeliveryDateDto> deliveryDateDtos = known.values().stream()
                .sorted(Comparator.comparing(DeliveryDateDto::getDate))
                .collect(Collectors.toList());

        return new PeriodDto(deliveryDateDtos, start, stop);
    }

    private List<LeadTimeDto> getRollingLeadTimes(int rolling,
            Map<LocalDate, List<LeadTimeDto>> leadTimesByDate, LocalDate date) {
        Set<LocalDate> rollingDates = IntStream.range(0, rolling)
                .mapToObj(i -> date.minus(i, ChronoUnit.DAYS))
                .collect(Collectors.toSet());

        return rollingDates.stream()
                .flatMap(rollingDate -> leadTimesByDate
                        .getOrDefault(rollingDate, Collections.emptyList())
                        .stream()).collect(Collectors.toList());
    }
}
