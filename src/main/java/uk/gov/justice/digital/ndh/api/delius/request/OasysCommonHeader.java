package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OasysCommonHeader {
    @JacksonXmlProperty(localName = "Version", namespace = "http://www.bconline.co.uk/oasys/common")
    private String version;
    @JacksonXmlProperty(localName = "MessageID", namespace = "http://www.bconline.co.uk/oasys/common")
    private String messageId;
}
