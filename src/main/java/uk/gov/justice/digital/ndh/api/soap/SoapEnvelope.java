package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JacksonXmlRootElement(localName = "Envelope", namespace = "http://www.w3.org/2003/05/soap-envelope")
public class SoapEnvelope {

    @JacksonXmlProperty(localName = "Header", namespace = "http://www.w3.org/2003/05/soap-envelope")
    @JsonProperty("Header")
    private SoapHeader header;

    @JacksonXmlProperty(localName = "Body", namespace = "http://www.w3.org/2003/05/soap-envelope")
    @JsonProperty("Body")
    private SoapBody body;
}
