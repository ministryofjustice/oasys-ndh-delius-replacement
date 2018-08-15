package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;

@Value
@Builder
public class RiskUpdateResponse {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/riskupdateresponse")
    private Header header;
    @JacksonXmlProperty(localName = "CaseReferenceNumber", namespace = "http://www.hp.com/NDH_Web_Service/riskupdateresponse")
    private String caseReferenceNumber;
}
