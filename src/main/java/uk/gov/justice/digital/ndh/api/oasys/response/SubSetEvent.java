package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.common.ICMSReference;

@Value
@Builder
public class SubSetEvent {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/subsetevent", localName = "EventNumber")
    private String eventNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/subsetevent", localName = "ICMSReference")
    private ICMSReference icmsReference;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/subsetevent", localName = "SentenceDate")
    private String sentenceDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/subsetevent", localName = "SentenceCode")
    private String sentenceCode;



}
