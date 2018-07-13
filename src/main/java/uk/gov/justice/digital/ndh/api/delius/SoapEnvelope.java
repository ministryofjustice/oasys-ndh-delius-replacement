package uk.gov.justice.digital.ndh.api.delius;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JacksonXmlRootElement(localName = "Envelope", namespace = "http://www.w3.org/2003/05/soap-envelope")
public class SoapEnvelope {
    @JacksonXmlProperty(localName = "Header", namespace = "http://www.w3.org/2003/05/soap-envelope")
    private SoapHeader header;
    @JacksonXmlProperty(localName = "Body", namespace = "http://www.w3.org/2003/05/soap-envelope")
    private SoapBody body;
}
