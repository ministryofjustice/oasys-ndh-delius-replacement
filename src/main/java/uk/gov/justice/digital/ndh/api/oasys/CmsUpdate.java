package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
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
