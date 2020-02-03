package ru.rt.eip.simpletariffs.validation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.rt.eip.simpletariffs.validation.dto.*;
import ru.rt.eip.simpletariffs.validation.service.LcrService;
import ru.rt.eip.simpletariffs.validation.service.ValidationService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional(transactionManager = "itcTransactionManager", readOnly = true)
public class Controller {

    private final LcrService lcrService;
    private final ValidationService validationService;

    @GetMapping(value = "/lcr-data-prepare")
    public ResponseEntity prepare(String fromDate, String toDate) {

        log.debug("prepare fromDate: " + fromDate + " toDate: " + toDate);

        lcrService.fillLcrTables();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/get-lcr-filters")
    public ResponseEntity<FiltersOptionsDto> getLcrFilters() {
        return ResponseEntity.ok(lcrService.getLcrFilters());
    }

    @PostMapping(value = "/get-lcr-data")
    public ResponseEntity<Page<LcrPrefixZoneInfoDto>> getLcrData(@RequestBody FiltersSelectedValuesPagingDto pagingFilter) {

        log.debug("pagingFilter: " + pagingFilter);

        Pageable pageable = PageRequest.of(pagingFilter.getPageNumber(),
                                           pagingFilter.getPageSize() != 0 ? pagingFilter.getPageSize() : 100);

        return ResponseEntity.ok(lcrService.getLcrData(pagingFilter, pageable));
    }

    @PostMapping(value = "/verify-lcr-by-prefixes")
    public ResponseEntity<List<ValidationPrefixResultDto>> verifyByPrefixes(
            @RequestBody FiltersSelectedValuesWithExcludedItemsDto filter) {

        log.debug("verifyByPrefixes filter: " + filter);

        return ResponseEntity.ok(validationService.verifyPrefixes(filter));
    }

    @PostMapping(value = "/verify-lcr-by-count")
    public ResponseEntity<List<ValidationCountResultDto>> verifyByCount(
            @RequestBody FiltersSelectedValuesWithExcludedItemsDto filter) {

        log.debug("verifyByCount filter: " + filter);

        return ResponseEntity.ok(validationService.verifyCount(filter));
    }

    @PostMapping(value = "/get-report-by-prefixes")
    public ResponseEntity<String> getReportByPrefixes(
            @RequestBody FiltersSelectedValuesWithExcludedItemsDto filter) {

/* вернуть 1Гб на фронт для тестирования как это будет обработано
        ... ResponseEntity<byte[]> ...
        ...
        StringBuilder builder = new StringBuilder();
        int size = 2 * 1024 * 1024; // 1Гб
        for (int i = 0; i < size; i++) {
            builder.append("01234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567012345670123456701234567");
        }
        return ResponseEntity.ok(builder.toString().getBytes());
*/

        log.debug("getReportByPrefixes filter: " + filter);

        List<ValidationPrefixResultDto> verifyPrefixes = validationService.verifyPrefixes(filter);

        String result = "price,operator,service,prefix,prefixInPrefZone,prefixZone,prefixZoneFromLcr,priceLcr,prefixZoneLcr,differenceType\r\n";

        result += verifyPrefixes.stream()
                                .map(i -> i.getPrice() +
                                        "," +
                                        i.getOperator() +
                                        "," +
                                        i.getService() +
                                        "," +
                                        i.getPrefix() +
                                        "," +
                                        i.getPrefixInPrefZone() +
                                        "," +
                                        i.getPrefixZone() +
                                        "," +
                                        i.getPrefixZoneFromLcr() +
                                        "," +
                                        i.getPriceLcr() +
                                        "," +
                                        i.getPrefixZoneLcr() +
                                        "," +
                                        i.getDifferenceType())
                                .collect(Collectors.joining("\r\n"));

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/get-report-by-count")
    public ResponseEntity<String> getReportByCount(
            @RequestBody FiltersSelectedValuesWithExcludedItemsDto filter) {

        log.debug("getReportByCount filter: " + filter);

        List<ValidationCountResultDto> verifyPrefixes = validationService.verifyCount(filter);

        String result = "prefixZoneItc,prefixZoneLcr,operator,service,countItc,countLcr\r\n";

        result += verifyPrefixes.stream()
                                .map(i -> i.getPrefixZoneItc() +
                                        "," +
                                        i.getPrefixZoneLcr() +
                                        "," +
                                        i.getOperator() +
                                        "," +
                                        i.getService() +
                                        "," +
                                        i.getCountItc() +
                                        "," +
                                        i.getCountLcr())
                                .collect(Collectors.joining("\r\n"));

        return ResponseEntity.ok(result);
    }

}