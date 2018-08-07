package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;


@Value
@Builder
public class GetSubSetOffenderDetailsResponse {
    @JsonProperty("SubSetOffender")
    private SubSetOffender subSetOffender;
    @JsonProperty("SubSetEvent")
    private List<SubSetEvent> subSetEvents;
}
