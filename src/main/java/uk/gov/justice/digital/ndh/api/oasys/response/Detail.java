package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Detail {
    @JacksonXmlProperty(localName = "AuthorisationFailureException", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private FaultDetail authorisationFailureException;
    @JacksonXmlProperty(localName = "BusinessException", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private FaultDetail businessException;
    @JacksonXmlProperty(localName = "ServerRuntimeException", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private FaultDetail serverRuntimeException;
    @JacksonXmlProperty(localName = "ValidationFailureException", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private FaultDetail validationFailureException;
}
