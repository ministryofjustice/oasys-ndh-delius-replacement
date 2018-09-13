package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Offences {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "OffenceGroupCode")
    private String offenceGroupCode;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "OffenceSubCode")
    private String offenceSubCode;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "AdditionalIndicator")
    private String additionalIndicator;
}
