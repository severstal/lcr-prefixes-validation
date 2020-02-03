package ru.rt.eip.simpletariffs.validation.dto;

import lombok.*;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode // used in Cacheable
public class FiltersSelectedValuesDto {

    private List<String> products;
    private List<String> prefixZones;
    private List<String> countries;
    private List<String> operators;
    private String prefix;
    private String fromDate;
    private String toDate;

}
