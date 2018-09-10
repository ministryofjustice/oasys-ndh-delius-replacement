package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import uk.gov.justice.digital.ndh.api.oasys.common.ICMSReference;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderDetailsResponseTest {

    @Test
    public void serilizedResponseIsSchemaCompliant() throws JsonProcessingException {
        final String expected = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:off=\"http://www.hp.com/NDH_Web_Service/Offender_Details_Response\" xmlns:dom=\"http://www.hp.com/NDH_Web_Service/DomainTypes\" xmlns:off1=\"http://www.hp.com/NDH_Web_Service/offender\" xmlns:even=\"http://www.hp.com/NDH_Web_Service/event\">\n" +
                "   <soap:Header/>\n" +
                "   <soap:Body>\n" +
                "      <off:OffenderDetailsResponse>\n" +
                "         <off:Header>\n" +
                "            <dom:ApplicationMode>?</dom:ApplicationMode>\n" +
                "            <dom:CorrelationID>?</dom:CorrelationID>\n" +
                "            <dom:OASysRUsername>?</dom:OASysRUsername>\n" +
                "            <dom:MessageTimestamp>?</dom:MessageTimestamp>\n" +
                "         </off:Header>\n" +
                "         <off1:OffenderDetail>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:CMSProbNumber>?</off1:CMSProbNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:PrisonNumber>?</off1:PrisonNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:NomisID>?</off1:NomisID>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:FamilyName>?</off1:FamilyName>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:Forename1>?</off1:Forename1>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:Forename2>?</off1:Forename2>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:Forename3>?</off1:Forename3>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:Gender>?</off1:Gender>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DateOfBirth>?</off1:DateOfBirth>\n" +
                "            <!--Zero or more repetitions:-->\n" +
                "            <off1:Alias>\n" +
                "               <!--Optional:-->\n" +
                "               <off1:AliasFamilyName>?</off1:AliasFamilyName>\n" +
                "               <!--Optional:-->\n" +
                "               <off1:AliasForename1>?</off1:AliasForename1>\n" +
                "               <!--Optional:-->\n" +
                "               <off1:AliasForename2>?</off1:AliasForename2>\n" +
                "               <!--Optional:-->\n" +
                "               <off1:AliasForename3>?</off1:AliasForename3>\n" +
                "               <!--Optional:-->\n" +
                "               <off1:AliasDateOfBirth>?</off1:AliasDateOfBirth>\n" +
                "            </off1:Alias>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:EthnicCategory>?</off1:EthnicCategory>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AddressLine1>?</off1:AddressLine1>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AddressLine2>?</off1:AddressLine2>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AddressLine3>?</off1:AddressLine3>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AddressLine4>?</off1:AddressLine4>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AddressLine5>?</off1:AddressLine5>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AddressLine6>?</off1:AddressLine6>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:PostCode>?</off1:PostCode>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:TelephoneNumber>?</off1:TelephoneNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:PNC>?</off1:PNC>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:CRONumber>?</off1:CRONumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:Language>?</off1:Language>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:Religion>?</off1:Religion>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:ReleaseDate>?</off1:ReleaseDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:ReleaseType>?</off1:ReleaseType>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:License>?</off1:License>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:LAOIndicator>?</off1:LAOIndicator>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:CellLocation>?</off1:CellLocation>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:SecurityCategory>?</off1:SecurityCategory>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeAddressLine1>?</off1:DischargeAddressLine1>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeAddressLine2>?</off1:DischargeAddressLine2>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeAddressLine3>?</off1:DischargeAddressLine3>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeAddressLine4>?</off1:DischargeAddressLine4>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeAddressLine5>?</off1:DischargeAddressLine5>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeAddressLine6>?</off1:DischargeAddressLine6>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargePostcode>?</off1:DischargePostcode>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:DischargeTelphoneNumber>?</off1:DischargeTelphoneNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AppealPendingIndicator>?</off1:AppealPendingIndicator>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:CurfewDate>?</off1:CurfewDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:ParoleEligibilityDate>?</off1:ParoleEligibilityDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:LicenceExpiryDate>?</off1:LicenceExpiryDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:SentenceExpiryDate>?</off1:SentenceExpiryDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:NonParoleDate>?</off1:NonParoleDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:AutomaticReleaseDate>?</off1:AutomaticReleaseDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:ConditionalReleaseDate>?</off1:ConditionalReleaseDate>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:RiskOfSelfHarm>?</off1:RiskOfSelfHarm>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:NumberOfOffences>?</off1:NumberOfOffences>\n" +
                "            <!--Optional:-->\n" +
                "            <off1:FirstConvictionAge>?</off1:FirstConvictionAge>\n" +
                "            <!--Zero or more repetitions:-->\n" +
                "            <off1:NOMCondition>?</off1:NOMCondition>\n" +
                "         </off1:OffenderDetail>\n" +
                "         <!--Optional:-->\n" +
                "         <even:EventDetail>\n" +
                "            <!--You have a CHOICE of the next 2 items at this level-->\n" +
                "            <even:EventNumber>?</even:EventNumber>\n" +
                "            <even:ICMSReference>\n" +
                "               <dom:RefClient>?</dom:RefClient>\n" +
                "               <dom:RefLink>?</dom:RefLink>\n" +
                "               <dom:RefSupervision>?</dom:RefSupervision>\n" +
                "            </even:ICMSReference>\n" +
                "            <!--Zero or more repetitions:-->\n" +
                "            <even:Offences>\n" +
                "               <!--Optional:-->\n" +
                "               <dom:OffenceGroupCode>?</dom:OffenceGroupCode>\n" +
                "               <!--Optional:-->\n" +
                "               <dom:OffenceSubCode>?</dom:OffenceSubCode>\n" +
                "               <!--Optional:-->\n" +
                "               <dom:AdditionalIndicator>?</dom:AdditionalIndicator>\n" +
                "            </even:Offences>\n" +
                "            <!--Optional:-->\n" +
                "            <even:ReleaseDate>?</even:ReleaseDate>\n" +
                "            <!--Optional:-->\n" +
                "            <even:SentenceCode>?</even:SentenceCode>\n" +
                "            <!--Optional:-->\n" +
                "            <even:SentenceDate>?</even:SentenceDate>\n" +
                "            <!--Optional:-->\n" +
                "            <even:OffenceDate>?</even:OffenceDate>\n" +
                "            <!--Optional:-->\n" +
                "            <even:SentenceLength>?</even:SentenceLength>\n" +
                "            <!--Optional:-->\n" +
                "            <even:ConbinedLength>?</even:ConbinedLength>\n" +
                "            <!--Optional:-->\n" +
                "            <even:CourtCode>?</even:CourtCode>\n" +
                "            <!--Optional:-->\n" +
                "            <even:CourtName>?</even:CourtName>\n" +
                "            <!--Optional:-->\n" +
                "            <even:CourtType>?</even:CourtType>\n" +
                "            <!--Zero or more repetitions:-->\n" +
                "            <even:SentenceDetail>\n" +
                "               <!--Optional:-->\n" +
                "               <even:AttributeCategory>?</even:AttributeCategory>\n" +
                "               <!--Optional:-->\n" +
                "               <even:AttributeElement>?</even:AttributeElement>\n" +
                "               <!--You have a CHOICE of the next 3 items at this level-->\n" +
                "               <!--Optional:-->\n" +
                "               <even:Description>?</even:Description>\n" +
                "               <!--Optional:-->\n" +
                "               <even:LengthInHours>?</even:LengthInHours>\n" +
                "               <!--Optional:-->\n" +
                "               <even:LengthInMonths>?</even:LengthInMonths>\n" +
                "            </even:SentenceDetail>\n" +
                "         </even:EventDetail>\n" +
                "      </off:OffenderDetailsResponse>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>";

        final SoapEnvelope oasysResponse = SoapEnvelope
                .builder()
                .header(SoapHeader
                        .builder()
                        .build())
                .body(SoapBody
                        .builder()
                        .offenderDetailsResponse(OffenderDetailsResponse
                                .builder()
                                .header(Header
                                        .builder()
                                        .messageTimestamp("?")
                                        .oasysRUsername("?")
                                        .correlationID("?")
                                        .applicationMode("?")
                                        .build())
                                .eventDetail(EventDetail
                                        .builder()
                                        .combinedLength("?")
                                        .courtCode("?")
                                        .courtName("?")
                                        .courtType("?")
                                        .eventNumber("?")
                                        .icmsReference(ICMSReference
                                                .builder()
                                                .refClient("?")
                                                .refLink("?")
                                                .refSupervision("?")
                                                .build())
                                        .offenceDate("?")
                                        .offences(ImmutableList.of(Offences
                                                .builder()
                                                .additionalIndicator("?")
                                                .offenceGroupCode("?")
                                                .offenceSubCode("?")
                                                .build()))
                                        .releaseDate("?")
                                        .sentenceCode("?")
                                        .sentenceDate("?")
                                        .sentenceDetails(ImmutableList.of(SentenceDetail
                                                .builder()
                                                .attributeCategory("?")
                                                .attributeElement("?")
                                                .description("?")
                                                .lengthInHours("?")
                                                .lengthInMonths("?")
                                                .build()))
                                        .sentenceLength("?")
                                        .build())
                                .offenderDetail(OffenderDetail
                                        .builder()
                                        .addressLine1("?")
                                        .addressLine2("?")
                                        .addressLine3("?")
                                        .addressLine4("?")
                                        .addressLine5("?")
                                        .addressLine6("?")
                                        .aliases(ImmutableList.of(Alias
                                                .builder()
                                                .aliasDateOfBirth("?")
                                                .aliasFamilyName("?")
                                                .aliasForename1("?")
                                                .aliasForename2("?")
                                                .aliasForename3("?")
                                                .build()))
                                        .appealPendingIndicator("?")
                                        .automaticReleaseDate("?")
                                        .cellLocation("?")
                                        .cmsProbNumber("?")
                                        .conditionalReleaseDate("?")
                                        .croNumber("?")
                                        .curfewDate("?")
                                        .dateOfBirth("?")
                                        .dischargeAddressLine1("?")
                                        .dischargeAddressLine2("?")
                                        .dischargeAddressLine3("?")
                                        .dischargeAddressLine4("?")
                                        .dischargeAddressLine5("?")
                                        .dischargeAddressLine6("?")
                                        .dischargePostCode("?")
                                        .dischargeTelephoneNumber("?")
                                        .ethnicCategory("?")
                                        .familyName("?")
                                        .firstConvictionAge("?")
                                        .forename1("?")
                                        .forename2("?")
                                        .forename3("?")
                                        .gender("?")
                                        .language("?")
                                        .laoIndicator("?")
                                        .licenceExpiryDate("?")
                                        .license("?")
                                        .nomConditions(ImmutableList.of("?"))
                                        .nomisId("?")
                                        .nonParoleDate("?")
                                        .numberOfOffences("?")
                                        .paroleEligibilityDate("?")
                                        .pnc("?")
                                        .postCode("?")
                                        .prisonNumber("?")
                                        .releaseDate("?")
                                        .releaseType("?")
                                        .religion("?")
                                        .riskOfSelfHarm("?")
                                        .securityCategory("?")
                                        .sentenceExpiryDate("?")
                                        .telephoneNumber("?")
                                        .build())
                                .build())
                        .build())
                .build();

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String actual = xmlMapper.writeValueAsString(oasysResponse);

        Diff myDiff = DiffBuilder.compare(actual).withTest(expected)
                .withDifferenceEvaluator(DifferenceEvaluators.Default)
                .ignoreComments()
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertThat(myDiff.hasDifferences()).isFalse();
    }

}