package ru.rt.eip.simpletariffs.validation.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class FiltersSelectedValuesWithExcludedItemsDto extends FiltersSelectedValuesDto {

    private List<String> excludedItems;

}
