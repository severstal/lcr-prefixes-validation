package ru.rt.eip.simpletariffs.validation.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rt.eip.simpletariffs.validation.dto.*;
import ru.rt.eip.simpletariffs.validation.repository.itc.ItcPrefixEntity;
import ru.rt.eip.simpletariffs.validation.repository.itc.ItcPrefixesCountEntity;
import ru.rt.eip.simpletariffs.validation.repository.itc.ItcRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "itcTransactionManager", readOnly = true)
public class ValidationService {

    private final ItcRepository itcRepository;
    private final LcrService lcrService;

    /**
     * сверка (по префиксам) данных LCR с данными Интерконнект
     *
     * @param filters фильтры для данных для сверки
     */
    public List<ValidationPrefixResultDto> verifyPrefixes(FiltersSelectedValuesWithExcludedItemsDto filters) {

        log.debug("filters: " + filters.toString());

        List<LcrPrefixZoneInfoDto> lcrData = lcrService.getLcrData(filters);
        log.debug("lcrData: " + lcrData.toString());

        lcrData = excludeItems(filters, lcrData);

        Map<LcrPrefixZoneInfoDto, List<LcrPrefixDto>> lcrPrefixesMap = buildLcrToPrefixesMap(lcrData);

        List<ValidationPrefixResultDto> result = new ArrayList<>();
        for (LcrPrefixZoneInfoDto lcr : lcrPrefixesMap.keySet()) {

            String zoneName = buildZoneName(lcr);
            log.debug("zoneName: " + zoneName);

            List<ItcPrefixEntity> itcPrefixes = itcRepository.findItcPrefixes(zoneName, filters.getFromDate());
            log.debug("itcPrefixes: " + itcPrefixes.toString());

            List<LcrPrefixDto> lcrPrefixes = lcrPrefixesMap.get(lcr);
            log.debug("lcrPrefixes: " + lcrPrefixes.toString());

            List<LcrPrefixDto> missingInItc =
                    lcrPrefixes.stream()
                               .filter(lcrPrefix -> itcPrefixes.stream()
                                                               .noneMatch(itcPrefix -> prefixEquals(itcPrefix, lcrPrefix)))
                               .collect(Collectors.toList());

            List<ItcPrefixEntity> missingInLcr =
                    itcPrefixes.stream()
                               .filter(itcPrefix -> lcrPrefixes.stream()
                                                               .noneMatch(lcrPrefix -> prefixEquals(itcPrefix, lcrPrefix)))
                               .collect(Collectors.toList());

            log.debug("missingInItc: " + missingInItc.toString());
            log.debug("missingInLcr: " + missingInLcr.toString());

            result.addAll(buildResult(lcr, missingInItc, missingInLcr));
        }

        result.sort(validationPrefixResultDtoComparator());
        result = addFakeIdForFront(result);
        return result;
    }

    /**
     * сверка (по кол-ву) данных LCR с данными Интерконнект
     *
     * @param filters фильтры для данных для сверки
     */
    public List<ValidationCountResultDto> verifyCount(FiltersSelectedValuesWithExcludedItemsDto filters) {

        log.debug("filters: " + filters.toString());

        List<LcrPrefixZoneInfoDto> lcrData = lcrService.getLcrData(filters);
        log.debug("lcrData: " + lcrData.toString());

        lcrData = excludeItems(filters, lcrData);

        Map<LcrPrefixZoneInfoDto, List<LcrPrefixDto>> lcrPrefixesMap = buildLcrToPrefixesMap(lcrData);

        List<ValidationCountResultDto> result = new ArrayList<>();
        long fakeIdForFront = 0;

        for (LcrPrefixZoneInfoDto lcr : lcrPrefixesMap.keySet()) {

            String zoneName = buildZoneName(lcr);
            log.debug("zoneName: " + zoneName);

            List<ItcPrefixesCountEntity> itcPrefixesCounts = itcRepository
                    .findItcPrefixesCount(zoneName, filters.getFromDate());
            log.debug("itcPrefixesCounts: " + itcPrefixesCounts.toString());

            List<LcrPrefixDto> lcrPrefixes = lcrPrefixesMap.get(lcr);
            log.debug("lcrPrefixes: " + lcrPrefixes.toString());

            if (itcPrefixesCounts.size() > 1) {
                log.debug("itcPrefixesCounts.size() > 1, only first will be used");
            }

            long lcrPrefixesCount = (long) lcrPrefixes.size();

            Optional<ItcPrefixesCountEntity> itcPrefixesCountFirst = itcPrefixesCounts.stream()
                                                                                      .findFirst();

            long itcPrefixesCount = itcPrefixesCountFirst.map(ItcPrefixesCountEntity::getPrefixCount)
                                                         .orElse(0L);

            String itcPrefixZone = itcPrefixesCountFirst.map(ItcPrefixesCountEntity::getPrefixZone)
                                                        .orElse("");

            if (!itcPrefixZone.isEmpty() && !itcPrefixZone.equals(zoneName)) {
                log.debug("itcPrefixZone not equals zoneName: " + itcPrefixZone + " " + zoneName);
            }

            if (lcrPrefixesCount != itcPrefixesCount) {
                log.debug("lcrPrefixesCount != itcPrefixesCount: " + lcrPrefixesCount + " " + itcPrefixesCount);

                String service = itcPrefixZone.isEmpty() ? "" : lcr
                        .getProduct();  // hb (BZN) d.LUX IDD (BZNStd) услуга это символы между 3м и 4м пробелами
                // но т.к. itcPrefixZone соотв заданному zoneName, которое строится по lcr, можно просто взять product

                result.add(ValidationCountResultDto.builder()
                                                   .id(fakeIdForFront++)
                                                   .operator(lcr.getOperator())
                                                   .prefixZoneItc(itcPrefixZone)
                                                   .prefixZoneLcr(lcr.getPrefixZone())
                                                   .service(service)
                                                   .countItc(itcPrefixesCount)
                                                   .countLcr(lcrPrefixesCount)
                                                   .build());
            }
        }
        return result;
    }

    private List<LcrPrefixZoneInfoDto> excludeItems(FiltersSelectedValuesWithExcludedItemsDto filters, List<LcrPrefixZoneInfoDto> lcrData) {
        Set<String> excludedItems = new HashSet<>();
        excludedItems.addAll(filters.getExcludedItems());

        lcrData = lcrData.stream()
                         .filter(i -> !excludedItems.contains(i.getId()))
                         .collect(Collectors.toList());

        log.debug("lcrData without excludedItems: " + lcrData.toString());
        return lcrData;
    }

    private Comparator<ValidationPrefixResultDto> validationPrefixResultDtoComparator() {
        return (o1, o2) -> {

            String o1Service = Optional.ofNullable(o1.getService()).orElse("");
            String o2Service = Optional.ofNullable(o2.getService()).orElse("");
            String o1Operator = Optional.ofNullable(o1.getOperator()).orElse("");
            String o2Operator = Optional.ofNullable(o2.getOperator()).orElse("");
            String o1PrefixZone = Optional.ofNullable(o1.getPrefixZone()).orElse("");
            String o2PrefixZone = Optional.ofNullable(o2.getPrefixZone()).orElse("");
            String o1Prefix = Optional.ofNullable(o1.getPrefix()).orElse("");
            String o2Prefix = Optional.ofNullable(o2.getPrefix()).orElse("");

            if (!o1Service.equals(o2Service)) {
                return o1Service.compareTo(o2Service);
            }

            if (!o1Operator.equals(o2Operator)) {
                return o1Operator.compareTo(o2Operator);
            }

            if (!o1PrefixZone.equals(o2PrefixZone)) {
                return o1PrefixZone.compareTo(o2PrefixZone);
            }

            if (!o1Prefix.equals(o2Prefix)) {
                return o1Prefix.compareTo(o2Prefix);
            }

            return 0;
        };
    }

    private List<ValidationPrefixResultDto> buildResult(LcrPrefixZoneInfoDto lcr, List<LcrPrefixDto> missingInItc, List<ItcPrefixEntity> missingInLcr) {

        List<ValidationPrefixResultDto> result = new ArrayList<>();

        long fakeIdForFront = 0;
        for (LcrPrefixDto lcrPrefix : missingInItc) {
            result.add(
                    ValidationPrefixResultDto.builder()
                                             .id(fakeIdForFront++)
                                             .price("")
                                             .operator(lcr.getOperator())
                                             .service(lcr.getProduct())
                                             .prefix(lcrPrefix.getPrefix().toString())
                                             .prefixInPrefZone("")
                                             .prefixZone(lcr.getPrefixZone())
                                             .prefixZoneFromLcr("")
                                             .prefixZoneLcr("")
                                             .priceLcr("")
                                             .differenceType("Отсутствует в Интерконнект")
                                             .build());
        }

        for (ItcPrefixEntity itcPrefix : missingInLcr) {
            result.add(
                    ValidationPrefixResultDto.builder()
                                             .id(fakeIdForFront++)
                                             .price("")
                                             .operator(lcr.getOperator())
                                             .service(lcr.getProduct())
                                             .prefix(itcPrefix.getPrefix().toString())
                                             .prefixInPrefZone("")
                                             .prefixZone(lcr.getPrefixZone())
                                             .prefixZoneFromLcr("")
                                             .prefixZoneLcr("")
                                             .priceLcr("")
                                             .differenceType("Отсутствует в LCR")
                                             .build());
        }
        return result;
    }

    private List<ValidationPrefixResultDto> addFakeIdForFront(List<ValidationPrefixResultDto> list) {
        long id = 0;
        for (ValidationPrefixResultDto dto : list) {
            dto.setId(id++);
        }
        return list;
    }

    private Map<LcrPrefixZoneInfoDto, List<LcrPrefixDto>> buildLcrToPrefixesMap(List<LcrPrefixZoneInfoDto> lcrData) {
        return lcrData.stream()
                      .collect(
                              Collectors.groupingBy(
                                      Function.identity(),
                                      Collectors.mapping(i -> LcrPrefixDto.builder()
                                                                          .prefix(i.getPrefix())
                                                                          .startDate(i.getFromDate())
                                                                          .endDate(i.getToDate())
                                                                          .build(),
                                                         Collectors.toList())));
    }

    private boolean prefixEquals(ItcPrefixEntity itcPrefix, LcrPrefixDto lcrPrefix) {
        return itcPrefix.getPrefix().equals(lcrPrefix.getPrefix())
                && itcLcrDateEquals(itcPrefix.getEndDate(), lcrPrefix.getEndDate());
    }

    // lcrDateMax = 9999-12-31, itcDateMax = 2999-12-31
    private boolean itcLcrDateEquals(Date itcDate, String lcrDateString) {
        String itcDateString = ItcLcrDateFormat.from(itcDate).format();
        return lcrDateString.equals(itcDateString) ||
                ("2999-12-31".equals(itcDateString) && "9999-12-31".equals(lcrDateString));
    }

    // 'hb (' + код оператора + ') d.' + код страны + пробел + услуга ITC + пробел + название преф зоны
    private String buildZoneName(LcrPrefixZoneInfoDto lcrEntity) {
        StringBuilder zoneName = new StringBuilder();
        zoneName.append("hb (")
                .append(lcrEntity.getOperator())
                .append(") d.")
                .append(lcrEntity.getCountry())
                .append(" ")
                .append(lcrEntity.getProduct())
                .append(" ")
                .append(lcrEntity.getPrefixZone());

        return zoneName.toString();
    }

    @Data
    @Builder
    static class LcrPrefixDto {

        private final String startDate;
        private final String endDate;
        private final BigDecimal prefix;

    }

}
