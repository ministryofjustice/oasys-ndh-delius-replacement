package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Custody {
    @JsonProperty("ReleaseDate")
    private String releaseDate;
    @JsonProperty("ReleaseType")
    private String releaseType;
    @JacksonXmlElementWrapper(localName = "LicenceConditions")
    @JsonProperty("LicenceConditions")
    private List<LicenceCondition> licenceConditions;
}
