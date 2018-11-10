package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class PhysicalAttributes {
    private String gender;
    private String ethnicity;
    private Integer heightFeet;
    private Integer heightInches;
    private BigDecimal heightMetres;
    private Integer heightCentimetres;
    private Integer weightPounds;
    private Integer weightKilograms;
}
