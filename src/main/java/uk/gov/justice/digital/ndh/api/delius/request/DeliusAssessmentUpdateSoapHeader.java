package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliusAssessmentUpdateSoapHeader {
    @JacksonXmlProperty(localName = "Header", namespace = "http://www.bconline.co.uk/oasys/common")
    private OasysCommonHeader commonHeader;
}
