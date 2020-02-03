package ru.rt.eip.simpletariffs.validation.repository.itc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedNativeQuery(name = "getItcPrefixesCount",
        query = "select distPrefix.prefixZone as prefixZone, \n" +
                "count(distPrefix.prefix)  as prefixCount \n" +
                "from ( \n" +
                "   select p_drcl_name as prefixZone, \n" +
                "   p_pset_prefix as prefix \n" +
                "   from s_api_directions \n" +
                "   where p_drcm_end_date > to_date(:fromDate, 'yyyy-mm-dd') \n" +
                "   group by p_drcl_name, p_pset_prefix ) distPrefix \n" +
                "group by distPrefix.prefixZone",
        resultSetMapping = "ItcPrefixesCountMapping"
)
@SqlResultSetMapping(
        name = "ItcPrefixesCountMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ItcPrefixesCountEntity.class,
                        columns = {
                                @ColumnResult(name = "prefixZone", type = String.class),
                                @ColumnResult(name = "prefixCount", type = long.class),
                        })
        })
public class ItcPrefixesCountEntity {

    @Id
    private String fakeId; // entity must have id
    private String prefixZone;
    private long prefixCount;

    /**
     * for @ConstructorResult
     */
    public ItcPrefixesCountEntity(String prefixZone, long prefixCount) {
        this.prefixZone = prefixZone;
        this.prefixCount = prefixCount;
    }

}


