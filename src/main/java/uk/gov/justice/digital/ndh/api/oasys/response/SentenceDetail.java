package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SentenceDetail {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "AttributeCategory")
    private String attributeCategory;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "AttributeElement")
    private String attributeElement;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "Description")
    private String description;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "LengthInHours")
    private String lengthInHours;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "LengthInMonths")
    private String lengthInMonths;
}
