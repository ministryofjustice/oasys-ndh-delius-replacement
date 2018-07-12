package uk.gov.justice.digital.ndh.api.delius;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentSummaryRequest {
    @JsonProperty("OASYSAssessmentSummary")
    private AssessmentSummary assessmentSummary;
    @JsonProperty("SupervisionPlan")
    private SupervisionPlan supervisionPlan;
    @JsonProperty("RiskType")
    private RiskType riskType;
}
