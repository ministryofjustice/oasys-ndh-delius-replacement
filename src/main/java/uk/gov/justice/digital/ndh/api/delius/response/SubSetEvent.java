package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubSetEvent {
    @JsonProperty("EventNumber")
    private String eventNumber;
    @JsonProperty("CommencementDate")
    private String commencementDate;
    @JsonProperty("OrderType")
    private String orderType;
}
