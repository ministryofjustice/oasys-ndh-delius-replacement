package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetOffenderDetailsResponse {
    @JsonProperty("Offender")
    private Offender offender;

    @JsonProperty("Event")
    private Event event;
}
