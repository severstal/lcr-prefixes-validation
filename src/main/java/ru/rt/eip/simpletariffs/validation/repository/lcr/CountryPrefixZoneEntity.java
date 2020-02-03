package ru.rt.eip.simpletariffs.validation.repository.lcr;

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
@NamedNativeQueries(value = {
        @NamedNativeQuery(name = "getCountryPrefixZones",
                query = "select distinct p.dest_country_iso_code country, coalesce(itc_dest_name, lcr_dest_name) prefixZone\n" +
                        "from lcr_itc_dest d, ext_exp_pdest p \n" +
                        "where d.native_id = p.destinationid",
                resultSetMapping = "CountryPrefixZoneMapping"
        )
})
@SqlResultSetMapping(
        name = "CountryPrefixZoneMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CountryPrefixZoneEntity.class,
                        columns = {
                                @ColumnResult(name = "country"),
                                @ColumnResult(name = "prefixZone"),
                        }
                )
        }
)
public class CountryPrefixZoneEntity {

    @Id
    private String fakeId; // entity must have id
    private String country;
    private String prefixZone;

    /**
     * for @ConstructorResult
     */
    public CountryPrefixZoneEntity(String country, String prefixZone) {
        this.country = country;
        this.prefixZone = prefixZone;
    }

}
