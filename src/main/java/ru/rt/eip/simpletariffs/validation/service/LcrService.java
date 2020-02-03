package ru.rt.eip.simpletariffs.validation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rt.eip.simpletariffs.validation.dto.FiltersOptionsDto;
import ru.rt.eip.simpletariffs.validation.dto.FiltersSelectedValuesDto;
import ru.rt.eip.simpletariffs.validation.dto.LcrPrefixZoneInfoDto;
import ru.rt.eip.simpletariffs.validation.repository.lcr.CountryPrefixZoneEntity;
import ru.rt.eip.simpletariffs.validation.repository.lcr.LcrPrefixZoneInfoEntity;
import ru.rt.eip.simpletariffs.validation.repository.lcr.LcrRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "lcrTransactionManager", readOnly = true)
public class LcrService {

    private final LcrRepository lcrRepository;

    /**
     * выполнить выгрузку (заполнение таблиц)
     */
    @Transactional(transactionManager = "lcrTransactionManager", readOnly = true)
    public void fillLcrTables() {
        lcrRepository.fillLcrTables();
    }

    /**
     * получить фильтры из LCR
     */
    @Transactional(transactionManager = "lcrTransactionManager", readOnly = true)
    public FiltersOptionsDto getLcrFilters() {

        List<String> products = lcrRepository.getProducts();
        List<String> operators = lcrRepository.getOperators();
        List<String> countries = lcrRepository.getCountries();
        List<String> prefixZones = lcrRepository.getPrefixZones();

        List<CountryPrefixZoneEntity> countryPrefixZones = lcrRepository.getCountryPrefixZones();
        Map<String, List<String>> countryPrefixZonesMap =
                countryPrefixZones.stream()
                                  .collect(Collectors.groupingBy(
                                          CountryPrefixZoneEntity::getCountry,
                                          Collectors.mapping(CountryPrefixZoneEntity::getPrefixZone,
                                                             Collectors.toList())));

        return FiltersOptionsDto.builder()
                                .products(products)
                                .operators(operators)
                                .countries(countries)
                                .prefixZones(prefixZones)
                                .country2PrefixZones(countryPrefixZonesMap)
                                .build();
    }

    /**
     * получить данные из LCR
     */
    @Transactional(transactionManager = "lcrTransactionManager", readOnly = true)
    public List<LcrPrefixZoneInfoDto> getLcrData(FiltersSelectedValuesDto filters) {

        log.debug("filters: " + filters.toString());

        List<LcrPrefixZoneInfoEntity> lcrData = lcrRepository.getLcrData(filters);

        log.debug("lcrData: " + lcrData.toString());

        return lcrData.stream()
                      .map(LcrPrefixZoneInfoDto::from)
                      .collect(Collectors.toList());
    }


    @Transactional(transactionManager = "lcrTransactionManager", readOnly = true)
    public Page<LcrPrefixZoneInfoDto> getLcrData(FiltersSelectedValuesDto filters, Pageable pageable) {

        log.debug("filters: " + filters.toString()
                          + " pageable.pageNumber:" + pageable.getPageNumber()
                          + " pageable.pageSize:" + pageable.getPageSize());

        List<LcrPrefixZoneInfoEntity> lcrData = lcrRepository
                .getLcrData(filters, pageable.getPageNumber(), pageable.getPageSize());

        List<LcrPrefixZoneInfoDto> lcrDataDtos = lcrData.stream()
                                                        .map(LcrPrefixZoneInfoDto::from)
                                                        .collect(Collectors.toList());

        long totalItemCount = lcrRepository.getLcrDataCount(filters);
        Page<LcrPrefixZoneInfoDto> page = new PageImpl<>(lcrDataDtos, pageable, totalItemCount);

        log.debug("lcrData: " + lcrData.toString());
        log.debug("page: " + page.toString());

        // сортировка услуга, оператор , страна, преф зона, префикс ? сортировка есть в запросе

        return page;
    }

}
