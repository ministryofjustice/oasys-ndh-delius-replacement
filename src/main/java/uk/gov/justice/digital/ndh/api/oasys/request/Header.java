package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Header {

    @JsonProperty("ApplicationMode")
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "ApplicationMode")
    private String applicationMode;

    @JsonProperty("CorrelationID")
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "CorrelationID")
    private String correlationID;

    @JsonProperty("OASysRUsername")
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "OASysRUsername")
    private String oasysRUsername;

    @JsonProperty("MessageTimestamp")
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "MessageTimestamp")
    private String messageTimestamp;

}
