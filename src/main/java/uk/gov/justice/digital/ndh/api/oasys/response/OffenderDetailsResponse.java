package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;

@Value
@Builder
public class OffenderDetailsResponse {

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/Offender_Details_Response", localName = "Header")
    private Header header;

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "OffenderDetail")
    private OffenderDetail offenderDetail;

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "EventDetail")
    private EventDetail eventDetail;

}
