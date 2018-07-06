package uk.gov.justice.digital.ndh.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CmsUpdate {
    @JsonProperty("Header")
    private Header header;
    @JsonProperty("Assessment")
    private Assessment assessment;
}
