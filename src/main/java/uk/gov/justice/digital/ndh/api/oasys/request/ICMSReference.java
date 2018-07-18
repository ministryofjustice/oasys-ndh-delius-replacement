package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ICMSReference {
    @JsonProperty("RefClient")
    private String refClient;
    @JsonProperty("RefLink")
    private String refLink;
    @JsonProperty("RefSupervision")
    private String refSupervision;
}
