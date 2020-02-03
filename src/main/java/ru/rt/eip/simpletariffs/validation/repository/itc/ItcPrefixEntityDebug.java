package ru.rt.eip.simpletariffs.validation.repository.itc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedNativeQuery(name = "getItcPrefixesDebug",
        query = "select trunc(p_drcm_start_date) as startDate, \n" +
                "trunc(p_drcm_end_date) as endDate, \n" +
                "p_pset_prefix as prefix, \n" +
                "p_drcl_name as prefixZone \n" +
                "from s_api_directions",
        resultSetMapping = "ItcPrefixMappingDebug"
)
@SqlResultSetMapping(
        name = "ItcPrefixMappingDebug",
        classes = {
                @ConstructorResult(
                        targetClass = ItcPrefixEntityDebug.class,
                        columns = {
                                @ColumnResult(name = "startDate", type = Date.class),
                                @ColumnResult(name = "endDate", type = Date.class),
                                @ColumnResult(name = "prefix", type = BigDecimal.class),
                                @ColumnResult(name = "prefixZone", type = String.class),
                        })
        })
public class ItcPrefixEntityDebug {

    @Id
    private String fakeId; // entity must have id
    private Date startDate;
    private Date endDate;
    private BigDecimal prefix;
    private String prefixZone;

    /**
     * for @ConstructorResult
     */
    public ItcPrefixEntityDebug(Date startDate, Date endDate, BigDecimal prefix, String prefixZone) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.prefix = prefix;
        this.prefixZone = prefixZone;
    }

}
