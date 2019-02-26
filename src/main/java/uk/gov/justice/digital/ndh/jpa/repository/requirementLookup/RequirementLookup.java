package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequirementLookup {
    private String reqType;
    private String reqCode;
    private String subCode;
    private String sentenceAttributeCat;
    private String sentenceAttributeElm;
    private String cjaUnpaidHours;
    private String cjaSupervisionMonths;
    private String activityDesc;

    public RequirementLookupKey getKey() {
        return RequirementLookupKey.builder()
                .reqCode(reqCode)
                .reqType(reqType)
                .subCode(subCode)
                .build();
    }
}
