package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Objective {
    @JsonProperty("ObjectiveDescription")
    private String objectiveDescription;
    @JsonProperty("ObjectiveStatus")
    private String objectiveStatus;
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
    @JsonProperty("ActionCode1")
    private String actionCode1;
    @JsonProperty("ActionText1")
    private String actionText1;
    @JsonProperty("ActionCode2")
    private String actionCode2;
    @JsonProperty("ActionText2")
    private String actionText2;
    @JsonProperty("ActionCode3")
    private String actionCode3;
    @JsonProperty("ActionText3")
    private String actionText3;

}
