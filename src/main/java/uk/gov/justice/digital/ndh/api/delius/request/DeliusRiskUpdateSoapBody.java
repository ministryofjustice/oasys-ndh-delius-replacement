package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliusRiskUpdateSoapBody {
    @JacksonXmlProperty(localName = "SubmitRiskDataRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private SubmitRiskDataRequest submitRiskDataRequest;
}
