package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Risk {
    @JsonProperty("RiskofHarm")
    private String RiskofHarm;
}
