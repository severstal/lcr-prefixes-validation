package ru.rt.eip.simpletariffs.validation.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class ValidationCountResultDto {

    private long id;
    private String prefixZoneItc;
    private String prefixZoneLcr;
    private String operator;
    private String service;
    private long countItc;
    private long countLcr;

}
