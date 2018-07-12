package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Offence {
    @JsonProperty("OffenceGroupCode")
    private String offenceGroupCode;
    @JsonProperty("OffenceSubCode")
    private String offenceSubCode;
    @JsonProperty("AdditionalIndicator")
    private String additionalIndicator;
}
