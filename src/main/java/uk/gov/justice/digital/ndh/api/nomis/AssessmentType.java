package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AssessmentType {
    private Long assessmentId;
    private String assessmentClass;
    private Long parentAssessmentId;
    private String assessmentCode;
    private String description;
    private Boolean cellSharingAlertFlag;
    private Boolean determineSupLevelFlag;
}
