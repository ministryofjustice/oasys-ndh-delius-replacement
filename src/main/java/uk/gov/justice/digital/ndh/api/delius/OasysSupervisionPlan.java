package uk.gov.justice.digital.ndh.api.delius;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OasysSupervisionPlan {
    @JsonProperty("CaseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("OASYS_ID")
    private String oasysId;
    @JsonProperty("ObjectiveNumber")
    private String objectiveNumber;
    @JsonProperty("Need1")
    private String need1;
    @JsonProperty("Need2")
    private String need2;
    @JsonProperty("Need3")
    private String need3;
    @JsonProperty("Need4")
    private String need4;
    @JsonProperty("Objective")
    private String objective;
    @JsonProperty("ObjectiveStatus")
    private String objectiveStatus;
    @JsonProperty("WorkSummary1")
    private String workSummary1;
    @JsonProperty("WorkSummary2")
    private String workSummary2;
    @JsonProperty("WorkSummary3")
    private String workSummary3;
    @JsonProperty("Text1")
    private String text1;
    @JsonProperty("Text2")
    private String text2;
    @JsonProperty("Text3")
    private String text3;
}
