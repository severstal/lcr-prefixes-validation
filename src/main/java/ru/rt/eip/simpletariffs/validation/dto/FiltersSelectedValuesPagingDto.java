package ru.rt.eip.simpletariffs.validation.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class FiltersSelectedValuesPagingDto extends FiltersSelectedValuesDto {

    private int pageNumber;
    private int pageSize;

}
