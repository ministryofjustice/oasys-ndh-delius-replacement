package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SoapEnvelope {
    @JsonProperty("Header")
    private SoapHeader header;
    @JsonProperty("Body")
    private SoapBody body;
}
