package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InitialSearchRequest {
    @JsonProperty("Header")
    private Header header;
    @JsonProperty("CMSProbNumber")
    private String cmsProbNumber;
    @JsonProperty("FamilyName")
    private String familyName;
    @JsonProperty("Forename1")
    private String forename1;
}
