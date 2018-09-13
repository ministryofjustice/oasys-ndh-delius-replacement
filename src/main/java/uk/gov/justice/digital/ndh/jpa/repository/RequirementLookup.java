package uk.gov.justice.digital.ndh.jpa.repository;

import lombok.Builder;
import lombok.Data;

@Data
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

}
