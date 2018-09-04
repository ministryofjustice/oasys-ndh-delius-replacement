package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;

import java.util.List;

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
         <off1:OffenderDetail>
            <!--Optional:-->
            <off1:CMSProbNumber>?</off1:CMSProbNumber>
            <!--Optional:-->
            <off1:PrisonNumber>?</off1:PrisonNumber>
            <!--Optional:-->
            <off1:NomisID>?</off1:NomisID>
            <!--Optional:-->
            <off1:FamilyName>?</off1:FamilyName>
            <!--Optional:-->
            <off1:Forename1>?</off1:Forename1>
            <!--Optional:-->
            <off1:Forename2>?</off1:Forename2>
            <!--Optional:-->
            <off1:Forename3>?</off1:Forename3>
            <!--Optional:-->
            <off1:Gender>?</off1:Gender>
            <!--Optional:-->
            <off1:DateOfBirth>?</off1:DateOfBirth>
            <!--Zero or more repetitions:-->
            <off1:Alias>
               <!--Optional:-->
               <off1:AliasFamilyName>?</off1:AliasFamilyName>
               <!--Optional:-->
               <off1:AliasForename1>?</off1:AliasForename1>
               <!--Optional:-->
               <off1:AliasForename2>?</off1:AliasForename2>
               <!--Optional:-->
               <off1:AliasForename3>?</off1:AliasForename3>
               <!--Optional:-->
               <off1:AliasDateOfBirth>?</off1:AliasDateOfBirth>
            </off1:Alias>
            <!--Optional:-->
            <off1:EthnicCategory>?</off1:EthnicCategory>
            <!--Optional:-->
            <off1:AddressLine1>?</off1:AddressLine1>
            <!--Optional:-->
            <off1:AddressLine2>?</off1:AddressLine2>
            <!--Optional:-->
            <off1:AddressLine3>?</off1:AddressLine3>
            <!--Optional:-->
            <off1:AddressLine4>?</off1:AddressLine4>
            <!--Optional:-->
            <off1:AddressLine5>?</off1:AddressLine5>
            <!--Optional:-->
            <off1:AddressLine6>?</off1:AddressLine6>
            <!--Optional:-->
            <off1:PostCode>?</off1:PostCode>
            <!--Optional:-->
            <off1:TelephoneNumber>?</off1:TelephoneNumber>
            <!--Optional:-->
            <off1:PNC>?</off1:PNC>
            <!--Optional:-->
            <off1:CRONumber>?</off1:CRONumber>
            <!--Optional:-->
            <off1:Language>?</off1:Language>
            <!--Optional:-->
            <off1:Religion>?</off1:Religion>
            <!--Optional:-->
            <off1:ReleaseDate>?</off1:ReleaseDate>
            <!--Optional:-->
            <off1:ReleaseType>?</off1:ReleaseType>
            <!--Optional:-->
            <off1:License>?</off1:License>
            <!--Optional:-->
            <off1:LAOIndicator>?</off1:LAOIndicator>
            <!--Optional:-->
            <off1:CellLocation>?</off1:CellLocation>
            <!--Optional:-->
            <off1:SecurityCategory>?</off1:SecurityCategory>
            <!--Optional:-->
            <off1:DischargeAddressLine1>?</off1:DischargeAddressLine1>
            <!--Optional:-->
            <off1:DischargeAddressLine2>?</off1:DischargeAddressLine2>
            <!--Optional:-->
            <off1:DischargeAddressLine3>?</off1:DischargeAddressLine3>
            <!--Optional:-->
            <off1:DischargeAddressLine4>?</off1:DischargeAddressLine4>
            <!--Optional:-->
            <off1:DischargeAddressLine5>?</off1:DischargeAddressLine5>
            <!--Optional:-->
            <off1:DischargeAddressLine6>?</off1:DischargeAddressLine6>
            <!--Optional:-->
            <off1:DischargePostcode>?</off1:DischargePostcode>
            <!--Optional:-->
            <off1:DischargeTelphoneNumber>?</off1:DischargeTelphoneNumber>
            <!--Optional:-->
            <off1:AppealPendingIndicator>?</off1:AppealPendingIndicator>
            <!--Optional:-->
            <off1:CurfewDate>?</off1:CurfewDate>
            <!--Optional:-->
            <off1:ParoleEligibilityDate>?</off1:ParoleEligibilityDate>
            <!--Optional:-->
            <off1:LicenceExpiryDate>?</off1:LicenceExpiryDate>
            <!--Optional:-->
            <off1:SentenceExpiryDate>?</off1:SentenceExpiryDate>
            <!--Optional:-->
            <off1:NonParoleDate>?</off1:NonParoleDate>
            <!--Optional:-->
            <off1:AutomaticReleaseDate>?</off1:AutomaticReleaseDate>
            <!--Optional:-->
            <off1:ConditionalReleaseDate>?</off1:ConditionalReleaseDate>
            <!--Optional:-->
            <off1:RiskOfSelfHarm>?</off1:RiskOfSelfHarm>
            <!--Optional:-->
            <off1:NumberOfOffences>?</off1:NumberOfOffences>
            <!--Optional:-->
            <off1:FirstConvictionAge>?</off1:FirstConvictionAge>
            <!--Zero or more repetitions:-->
            <off1:NOMCondition>?</off1:NOMCondition>
         </off1:OffenderDetail>
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
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/InitialSearchResponse")
    private Header header;

    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/subsetoffender", localName = "SubSetOffender")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SubSetOffender> subSetOffenders;

}
