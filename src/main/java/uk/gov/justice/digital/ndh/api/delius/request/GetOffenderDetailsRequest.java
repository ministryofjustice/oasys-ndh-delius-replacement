package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetOffenderDetailsRequest {
    @JacksonXmlProperty(namespace = "http://www.bconline.co.uk/oasys/messages", localName = "CaseReferenceNumber")
    private String caseReferenceNumber;
    @JacksonXmlProperty(namespace = "http://www.bconline.co.uk/oasys/messages", localName = "EventNumber")
    private String eventNumber;
    @JacksonXmlProperty(namespace = "http://www.bconline.co.uk/oasys/messages", localName = "NotesID")
    private String notesId;
}
