package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CmsUpdate {
    @JsonProperty("Header")
    private Header header;
    @JsonProperty("Assessment")
    private Assessment assessment;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("Objective")
    private List<Objective> objectives;

}
