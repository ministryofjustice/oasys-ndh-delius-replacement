package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Alias {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AliasFamilyName")
    private String aliasFamilyName;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AliasForename1")
    private String aliasForename1;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AliasForename2")
    private String aliasForename2;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AliasForename3")
    private String aliasForename3;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AliasDateOfBirth")
    private String aliasDateOfBirth;

}
