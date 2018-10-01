package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhysicalAttribute {
    private Long attributeSeq;
    private Integer heightFeet;
    private Integer heightInches;
    private Integer heightCm;
    private Integer weightLbs;
    private Integer weightKg;
}
