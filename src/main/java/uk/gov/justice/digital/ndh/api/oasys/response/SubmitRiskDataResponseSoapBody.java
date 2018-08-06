package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitRiskDataResponseSoapBody {
    @JacksonXmlProperty(localName = "RiskUpdateResponse", namespace = "http://www.hp.com/NDH_Web_Service/riskupdateresponse")
    private RiskUpdateResponse response;

}
