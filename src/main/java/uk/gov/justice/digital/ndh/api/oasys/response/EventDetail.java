package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.common.ICMSReference;

import java.util.List;

@Value
@Builder
public class EventDetail {

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "EventNumber")
    private String eventNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "ICMSReference")
    private ICMSReference icmsReference;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "Offences")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Offences> offences;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "ReleaseDate")
    private String releaseDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "SentenceCode")
    private String sentenceCode;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "SentenceDate")
    private String sentenceDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "OffenceDate")
    private String offenceDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "SentenceLength")
    private String sentenceLength;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "ConbinedLength")
    private String combinedLength;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "CourtCode")
    private String courtCode;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "CourtName")
    private String courtName;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "CourtType")
    private String courtType;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "SentenceDetail")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SentenceDetail> sentenceDetails;

}
