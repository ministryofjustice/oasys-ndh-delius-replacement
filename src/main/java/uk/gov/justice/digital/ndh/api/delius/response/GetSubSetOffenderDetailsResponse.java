package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Value;

import java.util.List;


@Value
@Builder
public class GetSubSetOffenderDetailsResponse {
    @JsonProperty("SubSetOffender")
    private SubSetOffender subSetOffender;
    @JsonProperty("SubSetEvent")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SubSetEvent> subSetEvents;
}
