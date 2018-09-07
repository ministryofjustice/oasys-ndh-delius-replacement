package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;

@Value
@Builder
public class OffenderDetailsResponse {
    /*
    <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:off="http://www.hp.com/NDH_Web_Service/Offender_Details_Response" xmlns:dom="http://www.hp.com/NDH_Web_Service/DomainTypes" xmlns:off1="http://www.hp.com/NDH_Web_Service/offender" xmlns:even="http://www.hp.com/NDH_Web_Service/event">
   <soap:Header/>
   <soap:Body>
      <off:OffenderDetailsResponse>
         <off:Header>
            <dom:ApplicationMode>?</dom:ApplicationMode>
            <dom:CorrelationID>?</dom:CorrelationID>
            <dom:OASysRUsername>?</dom:OASysRUsername>
            <dom:MessageTimestamp>?</dom:MessageTimestamp>
         </off:Header>

         <!--Optional:-->
         <even:EventDetail>
            <!--You have a CHOICE of the next 2 items at this level-->
            <even:EventNumber>?</even:EventNumber>
            <even:ICMSReference>
               <dom:RefClient>?</dom:RefClient>
               <dom:RefLink>?</dom:RefLink>
               <dom:RefSupervision>?</dom:RefSupervision>
            </even:ICMSReference>
            <!--Zero or more repetitions:-->
            <even:Offences>
               <!--Optional:-->
               <dom:OffenceGroupCode>?</dom:OffenceGroupCode>
               <!--Optional:-->
               <dom:OffenceSubCode>?</dom:OffenceSubCode>
               <!--Optional:-->
               <dom:AdditionalIndicator>?</dom:AdditionalIndicator>
            </even:Offences>
            <!--Optional:-->
            <even:ReleaseDate>?</even:ReleaseDate>
            <!--Optional:-->
            <even:SentenceCode>?</even:SentenceCode>
            <!--Optional:-->
            <even:SentenceDate>?</even:SentenceDate>
            <!--Optional:-->
            <even:OffenceDate>?</even:OffenceDate>
            <!--Optional:-->
            <even:SentenceLength>?</even:SentenceLength>
            <!--Optional:-->
            <even:ConbinedLength>?</even:ConbinedLength>
            <!--Optional:-->
            <even:CourtCode>?</even:CourtCode>
            <!--Optional:-->
            <even:CourtName>?</even:CourtName>
            <!--Optional:-->
            <even:CourtType>?</even:CourtType>
            <!--Zero or more repetitions:-->
            <even:SentenceDetail>
               <!--Optional:-->
               <even:AttributeCategory>?</even:AttributeCategory>
               <!--Optional:-->
               <even:AttributeElement>?</even:AttributeElement>
               <!--You have a CHOICE of the next 3 items at this level-->
               <!--Optional:-->
               <even:Description>?</even:Description>
               <!--Optional:-->
               <even:LengthInHours>?</even:LengthInHours>
               <!--Optional:-->
               <even:LengthInMonths>?</even:LengthInMonths>
            </even:SentenceDetail>
         </even:EventDetail>
      </off:OffenderDetailsResponse>
   </soap:Body>
</soap:Envelope>
     */
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/Offender_Details_Response", localName = "Header")
    private Header header;

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "OffenderDetail")
    private OffenderDetail offenderDetail;

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/event", localName = "EventDetail")
    private EventDetail eventDetail;

}
