package ru.rt.eip.simpletariffs.validation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltersOptionsDto {

    private List<String> products;
    private List<String> operators;
    private List<String> countries;
    private List<String> prefixZones;
    private Map<String, List<String>> country2PrefixZones;

}
