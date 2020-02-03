package ru.rt.eip.simpletariffs.validation.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class ValidationPrefixResultDto {

    private long id;
    private String price;
    private String operator;
    private String service;
    private String prefix;
    private String prefixInPrefZone;
    private String prefixZone;
    private String prefixZoneFromLcr;
    private String priceLcr;
    private String prefixZoneLcr;
    private String differenceType;

}
