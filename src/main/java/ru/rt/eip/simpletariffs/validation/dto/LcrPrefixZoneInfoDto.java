package ru.rt.eip.simpletariffs.validation.dto;

import lombok.*;
import ru.rt.eip.simpletariffs.validation.repository.lcr.LcrPrefixZoneInfoEntity;
import ru.rt.eip.simpletariffs.validation.service.ItcLcrDateFormat;

import java.math.BigDecimal;

@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // для мапы
@NoArgsConstructor
@AllArgsConstructor
public class LcrPrefixZoneInfoDto {

    private String id;
    private String price;

    @EqualsAndHashCode.Include
    private String operator;
    @EqualsAndHashCode.Include
    private String country;
    @EqualsAndHashCode.Include
    private String product;
    @EqualsAndHashCode.Include
    private String prefixZone;

    private BigDecimal prefix;
    private Boolean isHidden;
    private Boolean isExcluded;
    private String fromDate;
    private String toDate;

    public String getId() { // для фронта
        return price + operator + country + product + prefixZone + prefix + isHidden + isExcluded + fromDate + toDate;
    }

    public static LcrPrefixZoneInfoDto from(LcrPrefixZoneInfoEntity entity) {
        return LcrPrefixZoneInfoDto.builder()
                                   .operator(entity.getOperator())
                                   .country(entity.getCountry())
                                   .product(entity.getProduct())
                                   .prefixZone(entity.getPrefixZone())
                                   .prefix(entity.getPrefix())
                                   .isHidden(!entity.getIsHidden().equals(BigDecimal.ZERO))
                                   .fromDate(ItcLcrDateFormat.from(entity.getFromDate()).format())
                                   .toDate(ItcLcrDateFormat.from(entity.getToDate()).format())
                                   .build();
    }

}
