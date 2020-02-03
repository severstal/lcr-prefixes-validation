package ru.rt.eip.simpletariffs.validation.repository.lcr;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedNativeQueries(value = {
        @NamedNativeQuery(name = "getLcrData",
                query = "    select m.itc_carrier                                   as operator, \n" +
                        "    pref_zn.dest_country_iso_code                          as country, \n" +
                        "    m.itc_service                                          as product, \n" +
                        "    coalesce(dest.itc_dest_name, pref_zn.destination_name) as prefixzone, \n" +
                        "    coalesce(r.origin, r.area_digit)                       as prefix, \n" +
                        "       case \n" +
                        "    when r.valid_from <= to_date(:fromDate, 'yyyy-mm-dd') and \n" +
                        "    r.valid_until >= to_date(:toDate, 'yyyy-mm-dd') \n" +
                        "    then decode(r.modified, 2, 2, 1, 0, 0) \n" +
                        "         else 0 end                                        as isHidden, \n" +
                        "    r.valid_from                                           as fromDate, \n" +
                        "    r.valid_until                                          as toDate \n" +
                        "    from ext_exp_rdest_area_r2 r \n" +
                        "    join (select distinct itc_carrier, itc_service, lcrproduct_id \n" +
                        "                    from itc_service_map \n" +
                        "                  where approved = 'Y' \n" +
                        "                  and itc_service_map.agreement_type = 'HB') m on m.lcrproduct_id = r.productid \n" +
                        "    left join (select distinct dest_country_iso_code, destinationid, destination_name from ext_exp_pdest) pref_zn \n" +
                        "    on pref_zn.destinationid = r.pdestid \n" +
                        "    left join (select distinct itc_dest_name, country, itc_service, itc_carrier, native_id, is_bl \n" +
                        "            from lcr_itc_dest \n" +
                        "                       where agrtype = 'HB' \n" +
                        "                       and is_bl = 'N') dest on dest.country = pref_zn.dest_country_iso_code and \n" +
                        "    dest.itc_service = m.itc_service and \n" +
                        "    dest.itc_carrier = m.itc_carrier and \n" +
                        "    dest.native_id = r.pdestid \n" +
                        "  where \n" +
                        "      ( :productsDefined = 0 or m.itc_service in :products ) \n" +
                        "  and ( :countriesDefined = 0 or pref_zn.dest_country_iso_code in :countries ) \n" +
                        "  and ( :operatorsDefined = 0 or m.itc_carrier in :operators ) \n" +
                        "  and ( :prefixZonesDefined = 0 or coalesce(dest.itc_dest_name, pref_zn.destination_name) in :prefixZones ) \n" +
                        "  and ( :prefix is null or to_char(coalesce(r.origin, r.area_digit)) = :prefix )" +
                        "    order by 3, 1, 2, 4, 5 \n",
                resultSetMapping = "LcrEntityMapping"
        ),
        @NamedNativeQuery(name = "getLcrDataCount",
                query = "select count(*) \n" +
                        "    from ext_exp_rdest_area_r2 r \n" +
                        "    join (select distinct itc_carrier, itc_service, lcrproduct_id \n" +
                        "                    from itc_service_map \n" +
                        "                  where approved = 'Y' \n" +
                        "                  and itc_service_map.agreement_type = 'HB') m on m.lcrproduct_id = r.productid \n" +
                        "    left join (select distinct dest_country_iso_code, destinationid, destination_name from ext_exp_pdest) pref_zn \n" +
                        "    on pref_zn.destinationid = r.pdestid \n" +
                        "    left join (select distinct itc_dest_name, country, itc_service, itc_carrier, native_id, is_bl \n" +
                        "            from lcr_itc_dest \n" +
                        "                       where agrtype = 'HB' \n" +
                        "                       and is_bl = 'N') dest on dest.country = pref_zn.dest_country_iso_code and \n" +
                        "    dest.itc_service = m.itc_service and \n" +
                        "    dest.itc_carrier = m.itc_carrier and \n" +
                        "    dest.native_id = r.pdestid \n" +
                        "  where \n" +
                        "      ( :productsDefined = 0 or m.itc_service in :products ) \n" +
                        "  and ( :countriesDefined = 0 or pref_zn.dest_country_iso_code in :countries ) \n" +
                        "  and ( :operatorsDefined = 0 or m.itc_carrier in :operators ) \n" +
                        "  and ( :prefixZonesDefined = 0 or coalesce(dest.itc_dest_name, pref_zn.destination_name) in :prefixZones ) \n" +
                        "  and ( :prefix is null or to_char(coalesce(r.origin, r.area_digit)) = :prefix )"
        )
})
@SqlResultSetMapping(
        name = "LcrEntityMapping",
        classes = {
                @ConstructorResult(
                        targetClass = LcrPrefixZoneInfoEntity.class,
                        columns = {
                                @ColumnResult(name = "operator"),
                                @ColumnResult(name = "country"),
                                @ColumnResult(name = "product"),
                                @ColumnResult(name = "prefixZone"),
                                @ColumnResult(name = "prefix", type = BigDecimal.class),
                                @ColumnResult(name = "isHidden", type = BigDecimal.class),
                                @ColumnResult(name = "fromDate", type = Date.class),
                                @ColumnResult(name = "toDate", type = Date.class),
                        }
                )
        }
)
public class LcrPrefixZoneInfoEntity {

    @Id
    private String fakeId; // entity must have id
    private String operator;
    private String country;
    private String product;
    private String prefixZone;
    private BigDecimal prefix;
    private BigDecimal isHidden;
    private Date fromDate;
    private Date toDate;

    /**
     * for @ConstructorResult
     */
    public LcrPrefixZoneInfoEntity(String operator, String country, String product, String prefixZone, BigDecimal prefix,
                                   BigDecimal isHidden, Date fromDate, Date toDate) {
        this.operator = operator;
        this.country = country;
        this.product = product;
        this.prefixZone = prefixZone;
        this.prefix = prefix;
        this.isHidden = isHidden;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

/* исходный запрос

    select m.itc_carrier                                          as operator,                                                -- оператор
    pref_zn.dest_country_iso_code                          as country,                                                 -- страна
    m.itc_service                                          as product,                                                 -- продукт
    coalesce(dest.itc_dest_name, pref_zn.destination_name) as prefixzone,                                              -- преф. зона,
    coalesce(r.origin, r.area_digit)                       as prefix,                                                  -- префикс
       case
    when r.valid_from < = to_date('01.09.2018 00:00:00', 'dd.mm.yyyy hh24:mi:ss') and
    r.valid_until >= to_date('30.09.2018 00:00:00', 'dd.mm.yyyy hh24:mi:ss')
    then decode(r.modified, 2, 2, 1, 0, 0)
         else 0 end                                           as ishidden,                                                -- свернут (1/0)
    r.valid_from                                           as fromdate,                                                -- действует с
    r.valid_until                                          as todate                                                   -- действует по
    from ext_exp_rdest_area_r2 r
    join (select distinct itc_carrier, itc_service, lcrproduct_id
                    from itc_service_map
                  where approved = 'y'
                  and itc_service_map.agreement_type = 'hb') m on m.lcrproduct_id = r.productid
    left join (select distinct dest_country_iso_code, destinationid, destination_name from ext_exp_pdest) pref_zn
    on pref_zn.destinationid = r.pdestid
    left join (select distinct itc_dest_name, country, itc_service, itc_carrier, native_id, is_bl
            from lcr_itc_dest
                       where agrtype = 'hb'
                       and is_bl = 'n') dest on dest.country = pref_zn.dest_country_iso_code and
    dest.itc_service = m.itc_service and
    dest.itc_carrier = m.itc_carrier and
    dest.native_id = r.pdestid
    order by 1, 3, 2, 4, 5

*/
}
