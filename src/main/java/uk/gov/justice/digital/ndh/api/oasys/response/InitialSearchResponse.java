package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;

import java.util.List;

@Value
@Builder
public class InitialSearchResponse {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/InitialSearchResponse")
    private Header header;

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/subsetoffender", localName = "SubSetOffender")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SubSetOffender> subSetOffenders;

}
