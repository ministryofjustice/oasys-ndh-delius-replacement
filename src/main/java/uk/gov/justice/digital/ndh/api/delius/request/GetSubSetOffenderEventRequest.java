package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetSubSetOffenderEventRequest {
    @JacksonXmlProperty(localName = "CaseReferenceNumber", namespace = "http://www.bconline.co.uk/oasys/messages")
    private String caseReferenceNumber;
    @JacksonXmlProperty(localName = "Surname", namespace = "http://www.bconline.co.uk/oasys/messages")
    private String surname;
    @JacksonXmlProperty(localName = "Forename1", namespace = "http://www.bconline.co.uk/oasys/messages")
    private String forename1;
    @JacksonXmlProperty(localName = "NoteID", namespace = "http://www.bconline.co.uk/oasys/messages")
    private String noteId;

}
