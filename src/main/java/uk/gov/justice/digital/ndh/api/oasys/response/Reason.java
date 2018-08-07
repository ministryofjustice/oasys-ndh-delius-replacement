package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Reason {
    @JacksonXmlProperty(localName = "Text", namespace = "http://www.w3.org/2003/05/soap-envelope")
    private String text;
}
