package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JacksonXmlRootElement(localName = "Envelope", namespace = "http://www.w3.org/2003/05/soap-envelope")
public class DeliusRiskUpdateSoapEnvelope {
    @JacksonXmlProperty(localName = "Header", namespace = "http://www.w3.org/2003/05/soap-envelope")
    private DeliusRiskUpdateSoapHeader header;
    @JacksonXmlProperty(localName = "Body", namespace = "http://www.w3.org/2003/05/soap-envelope")
    private DeliusRiskUpdateSoapBody body;

}
