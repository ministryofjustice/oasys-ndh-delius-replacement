package uk.gov.justice.digital.ndh.api.oasys.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ICMSReference {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "RefClient")
    @JsonProperty("RefClient")
    private String refClient;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "RefLink")
    @JsonProperty("RefLink")
    private String refLink;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/DomainTypes", localName = "RefSupervision")
    @JsonProperty("RefSupervision")
    private String refSupervision;
}
