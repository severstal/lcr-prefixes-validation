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
@NamedNativeQuery(name = "getItcPrefixes",
        query = "select trunc(min(p_drcm_start_date)) as startDate, \n" +
                "trunc(max(p_drcm_end_date)) as endDate, \n" +
                "p_pset_prefix as prefix \n" +
                "from s_api_directions \n" +
                "where p_drcm_end_date > to_date(:fromDate, 'yyyy-mm-dd') \n" +
                "group by p_pset_prefix",
        resultSetMapping = "ItcPrefixMapping"
)
@SqlResultSetMapping(
        name = "ItcPrefixMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ItcPrefixEntity.class,
                        columns = {
                                @ColumnResult(name = "startDate", type = Date.class),
                                @ColumnResult(name = "endDate", type = Date.class),
                                @ColumnResult(name = "prefix", type = BigDecimal.class),
                        })
        })
public class ItcPrefixEntity {

    @Id
    private String fakeId; // entity must have id
    private Date startDate;
    private Date endDate;
    private BigDecimal prefix;

    /**
     * for @ConstructorResult
     */
    public ItcPrefixEntity(Date startDate, Date endDate, BigDecimal prefix) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.prefix = prefix;
    }

}
