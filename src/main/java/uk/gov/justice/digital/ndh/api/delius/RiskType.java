package uk.gov.justice.digital.ndh.api.delius;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskType {
    @JsonProperty("CaseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("RiskOfHarm")
    private String riskOfHarm;
}
