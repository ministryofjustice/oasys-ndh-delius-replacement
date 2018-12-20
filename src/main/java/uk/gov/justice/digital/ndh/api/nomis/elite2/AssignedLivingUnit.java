package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AssignedLivingUnit {
    private String agencyId;
    private Long locationId;
    private String description;
    private String agencyName;
}
