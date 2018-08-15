package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class    SubmitRiskDataRequest {
    @JacksonXmlProperty(localName = "RiskType", namespace = "http://www.bconline.co.uk/oasys/risk")
    private RiskType risk;
}
