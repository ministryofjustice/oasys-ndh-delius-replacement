package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Header {

    @JsonProperty("ApplicationMode")
    private String applicationMode;

    @JsonProperty("CorrelationID")
    private String correlationID;

    @JsonProperty("OASysRUsername")
    private String oasysRUsername;

    @JsonProperty("MessageTimestamp")
    private String messageTimestamp;

}
