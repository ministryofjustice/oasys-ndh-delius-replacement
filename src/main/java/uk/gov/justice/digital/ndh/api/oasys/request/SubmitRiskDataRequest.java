package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubmitRiskDataRequest {
    private Header header;
    @JsonProperty("CMSProbNumber")
    private String cmsProbNumber;
    @JsonProperty("Risk")
    private Risk risk;
    @JsonProperty("RiskFlags")
    private String riskFlags;
}
