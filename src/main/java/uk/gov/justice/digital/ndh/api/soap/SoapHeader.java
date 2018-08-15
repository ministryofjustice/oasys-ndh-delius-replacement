package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.delius.request.Header;

@Value
@Builder
public class SoapHeader {
    public String correlationId;

    @JacksonXmlProperty(localName = "Header", namespace = "http://www.bconline.co.uk/oasys/common")
    private Header header;

}
