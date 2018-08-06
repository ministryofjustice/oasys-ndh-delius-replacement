package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaultDetail {
    @JacksonXmlProperty(localName = "Code", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private String code;
    @JacksonXmlProperty(localName = "Description", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private String description;
    @JacksonXmlProperty(localName = "Timestamp", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private String timestamp;
    @JacksonXmlProperty(localName = "RequestMessage", namespace = "http://www.hp.com/NDH_Web_Service/Fault")
    private String requestMessage;
}
