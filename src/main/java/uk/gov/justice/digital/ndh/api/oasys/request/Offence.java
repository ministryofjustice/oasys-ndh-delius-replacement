package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Offence {
    @JsonProperty("OffenceGroupCode")
    private String offenceGroupCode;
    @JsonProperty("OffenceSubCode")
    private String offenceSubCode;
    @JsonProperty("AdditionalIndicator")
    private String additionalIndicator;
}
