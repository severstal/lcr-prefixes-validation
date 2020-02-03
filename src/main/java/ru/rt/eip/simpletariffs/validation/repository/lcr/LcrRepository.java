package ru.rt.eip.simpletariffs.validation.repository.lcr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import ru.rt.eip.simpletariffs.validation.dto.FiltersSelectedValuesDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class LcrRepository {

    @PersistenceContext(unitName = "lcrEntityManager")
    private EntityManager entityManager;

    public List<String> getProducts() {
        return entityManager.createNativeQuery("select distinct service_name from trf_service")
                            .getResultList();
    }

    public List<String> getOperators() {
        return entityManager.createNativeQuery("select distinct itc_carrier from itc_service_map ")
                            .getResultList();
    }

    public List<String> getCountries() {
        return entityManager.createNativeQuery("select distinct iso_country_code from ext_exp_country")
                            .getResultList();
    }

    public List<String> getPrefixZones() {
        return entityManager.createNativeQuery(
                "select distinct coalesce(itc_dest_name, lcr_dest_name) from lcr_itc_dest d, ext_exp_pdest p " +
                        "where d.native_id = p.destinationid")
                            .getResultList();
    }

    public List<CountryPrefixZoneEntity> getCountryPrefixZones() {
        return entityManager.createNamedQuery("getCountryPrefixZones", CountryPrefixZoneEntity.class)
                            .getResultList();
    }

    public List<LcrPrefixZoneInfoEntity> getLcrData(FiltersSelectedValuesDto filters, int pageNumber, int pageSize) {
        Query query = createLcrDataQuery(filters);

        query.setFirstResult(pageNumber * pageSize)
             .setMaxResults(pageSize);

        return query.getResultList();
    }

    public List<LcrPrefixZoneInfoEntity> getLcrData(FiltersSelectedValuesDto filters) {
        return createLcrDataQuery(filters).getResultList();
    }

    private Query createLcrDataQuery(FiltersSelectedValuesDto filters) {
        Query query = entityManager.createNamedQuery("getLcrData", LcrPrefixZoneInfoEntity.class);

        // todo may be check date format
        Optional.ofNullable(filters.getFromDate())
                .ifPresent(date -> query.setParameter("fromDate", date));

        Optional.ofNullable(filters.getToDate())
                .ifPresent(date -> query.setParameter("toDate", date));

        if (filters.getProducts().isEmpty()) {
            query.setParameter("productsDefined", 0)
                 .setParameter("products", null);
        } else {
            query.setParameter("productsDefined", 1)
                 .setParameter("products", filters.getProducts());
        }

        if (filters.getCountries().isEmpty()) {
            query.setParameter("countriesDefined", 0)
                 .setParameter("countries", null);
        } else {
            query.setParameter("countriesDefined", 1)
                 .setParameter("countries", filters.getCountries());
        }

        if (filters.getOperators().isEmpty()) {
            query.setParameter("operatorsDefined", 0)
                 .setParameter("operators", null);
        } else {
            query.setParameter("operatorsDefined", 1)
                 .setParameter("operators", filters.getOperators());
        }

        if (filters.getPrefixZones().isEmpty()) {
            query.setParameter("prefixZonesDefined", 0)
                 .setParameter("prefixZones", null);
        } else {
            query.setParameter("prefixZonesDefined", 1)
                 .setParameter("prefixZones", filters.getPrefixZones());
        }

        query.setParameter("prefix", filters.getPrefix().isEmpty() ? null : filters.getPrefix());

        return query;
    }

    @Cacheable("lcrDataCount")
    public long getLcrDataCount(FiltersSelectedValuesDto filters) {

        log.debug("getLcrDataCount for filters: " + filters.toString());
        log.debug("filters is not cached");

        Query query = entityManager.createNamedQuery("getLcrDataCount");

        if (filters.getProducts().isEmpty()) {
            query.setParameter("productsDefined", 0)
                 .setParameter("products", null);
        } else {
            query.setParameter("productsDefined", 1)
                 .setParameter("products", filters.getProducts());
        }

        if (filters.getCountries().isEmpty()) {
            query.setParameter("countriesDefined", 0)
                 .setParameter("countries", null);
        } else {
            query.setParameter("countriesDefined", 1)
                 .setParameter("countries", filters.getCountries());
        }

        if (filters.getOperators().isEmpty()) {
            query.setParameter("operatorsDefined", 0)
                 .setParameter("operators", null);
        } else {
            query.setParameter("operatorsDefined", 1)
                 .setParameter("operators", filters.getOperators());
        }

        if (filters.getPrefixZones().isEmpty()) {
            query.setParameter("prefixZonesDefined", 0)
                 .setParameter("prefixZones", null);
        } else {
            query.setParameter("prefixZonesDefined", 1)
                 .setParameter("prefixZones", filters.getPrefixZones());
        }

        query.setParameter("prefix", filters.getPrefix().isEmpty() ? null : filters.getPrefix());

        BigDecimal totalCount = (BigDecimal) query.getSingleResult();

        return totalCount.longValue();
    }

    @CacheEvict(allEntries = true, value = {"lcrDataCount"})
    @Scheduled(fixedDelay = 120 * 60 * 1000 ,  initialDelay = 500)
    public void reportCacheEvict() {
        log.debug("Flush cache");
    }

    public void fillLcrTables() {
        /*
        StoredProcedureQuery procedureQuery = entityManager
                .createStoredProcedureQuery("pkg_verif_tariff.import_dests_refresh")
                .registerStoredProcedureParameter(1,
                                                  LocalDateTime.class,
                                                  ParameterMode.IN)
                .registerStoredProcedureParameter(2,
                                                  LocalDateTime.class,
                                                  ParameterMode.IN)
                .registerStoredProcedureParameter("out",
                                                  Long.class,
                                                  ParameterMode.OUT)
                .setParameter(1, LocalDateTime.now().minus(500, ChronoUnit.DAYS))
                .setParameter(2, LocalDateTime.now().minus(200, ChronoUnit.DAYS));

        try {
            procedureQuery.execute();
            Long out = (Long) procedureQuery.getOutputParameterValue("out");
            System.out.println("procedure result:" + out);
        } finally {
            procedureQuery.unwrap(ProcedureOutputs.class)
                          .release();
        }

        TypedQuery<ResultClass> query = entityManager.createQuery(ru.rt.simpletariffs.Sql.select, ResultClass.class);
        List<ResultClass> resultList = query.getResultList();

*/
    }
}
