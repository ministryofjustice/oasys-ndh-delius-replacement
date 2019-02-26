package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequirementLookupKey {
    private String reqType;
    private String reqCode;
    private String subCode;


}
