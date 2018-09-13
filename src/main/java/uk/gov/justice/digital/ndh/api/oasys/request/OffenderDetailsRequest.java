package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.common.ICMSReference;

@Value
@Builder
public class OffenderDetailsRequest {
    @JsonProperty("header")
    private Header header;
    @JsonProperty("CMSProbNumber")
    private String cmsProbNumber;
    @JsonProperty("EventNumber")
    private String eventNumber;
    @JsonProperty("ICMSReference")
    private ICMSReference icmsReference;
    @JsonProperty("NomisID")
    private String nomisId;
    @JsonProperty("PrisonNumber")
    private String prisonNumber;
}
