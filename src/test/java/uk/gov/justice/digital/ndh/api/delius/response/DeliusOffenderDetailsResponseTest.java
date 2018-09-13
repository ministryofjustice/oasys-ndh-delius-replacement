package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliusOffenderDetailsResponseTest {

    @Test
    public void canDeserializeSoapResponse() throws IOException {

        final String soap = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:com=\"http://www.bconline.co.uk/oasys/common\" xmlns:mes=\"http://www.bconline.co.uk/oasys/messages\" xmlns:off=\"http://www.bconline.co.uk/oasys/offender\" xmlns:even=\"http://www.bconline.co.uk/oasys/event\">\n" +
                "   <soap:Header>\n" +
                "      <com:Header>\n" +
                "         <com:Version>?</com:Version>\n" +
                "         <com:MessageID>?</com:MessageID>\n" +
                "      </com:Header>\n" +
                "   </soap:Header>\n" +
                "   <soap:Body>\n" +
                "      <mes:GetOffenderDetailsResponse>\n" +
                "         <mes:Offender>\n" +
                "            <off:LAOIndicator>?</off:LAOIndicator>\n" +
                "            <off:CaseReferenceNumber>?</off:CaseReferenceNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off:PoliceNationalComputerIdentifier>?</off:PoliceNationalComputerIdentifier>\n" +
                "            <!--Optional:-->\n" +
                "            <off:LastName>?</off:LastName>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Forename1>?</off:Forename1>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Forename2>?</off:Forename2>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Forename3>?</off:Forename3>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Alias>?</off:Alias>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Gender>?</off:Gender>\n" +
                "            <!--Optional:-->\n" +
                "            <off:DateOfBirth>?</off:DateOfBirth>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Language>?</off:Language>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Religion>?</off:Religion>\n" +
                "            <!--Optional:-->\n" +
                "            <off:CRO>?</off:CRO>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Ethnicity>?</off:Ethnicity>\n" +
                "            <!--Optional:-->\n" +
                "            <off:MainAddress>\n" +
                "               <!--Optional:-->\n" +
                "               <off:AddressFirstLine>\n" +
                "                  <!--You may enter the following 2 items in any order-->\n" +
                "                  <!--Optional:-->\n" +
                "                  <off:BuildingName>?</off:BuildingName>\n" +
                "                  <!--Optional:-->\n" +
                "                  <off:HouseNumber>?</off:HouseNumber>\n" +
                "               </off:AddressFirstLine>\n" +
                "               <!--Optional:-->\n" +
                "               <off:StreetName>?</off:StreetName>\n" +
                "               <!--Optional:-->\n" +
                "               <off:District>?</off:District>\n" +
                "               <!--Optional:-->\n" +
                "               <off:TownOrCity>?</off:TownOrCity>\n" +
                "               <!--Optional:-->\n" +
                "               <off:County>?</off:County>\n" +
                "               <!--Optional:-->\n" +
                "               <off:TelephoneNumber>?</off:TelephoneNumber>\n" +
                "            </off:MainAddress>\n" +
                "            <!--Optional:-->\n" +
                "            <off:PrisonNumber>?</off:PrisonNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <off:Postcode>?</off:Postcode>\n" +
                "            <off:Telephone>?</off:Telephone>\n" +
                "         </mes:Offender>\n" +
                "         <mes:Event>\n" +
                "            <even:EventNumber>?</even:EventNumber>\n" +
                "            <!--Optional:-->\n" +
                "            <even:OffenceCode>?</even:OffenceCode>\n" +
                "            <!--Optional:-->\n" +
                "            <even:OffenceDate>?</even:OffenceDate>\n" +
                "            <!--Optional:-->\n" +
                "            <even:CommencementDate>?</even:CommencementDate>\n" +
                "            <!--Optional:-->\n" +
                "            <even:OrderType>?</even:OrderType>\n" +
                "            <!--Optional:-->\n" +
                "            <even:OrderLength>?</even:OrderLength>\n" +
                "            <!--Optional:-->\n" +
                "            <even:Court>?</even:Court>\n" +
                "            <!--Optional:-->\n" +
                "            <even:CourtType>?</even:CourtType>\n" +
                "            <!--Optional:-->\n" +
                "            <even:UWHours>?</even:UWHours>\n" +
                "            <!--Optional:-->\n" +
                "            <even:Requirements>\n" +
                "               <!--1 or more repetitions:-->\n" +
                "               <even:Requirement>\n" +
                "                  <even:Code>?</even:Code>\n" +
                "                  <even:MainCategory>?</even:MainCategory>\n" +
                "                  <even:SubCategory>?</even:SubCategory>\n" +
                "                  <!--Optional:-->\n" +
                "                  <even:RequirementDetails>\n" +
                "                     <even:Length>?</even:Length>\n" +
                "                  </even:RequirementDetails>\n" +
                "               </even:Requirement>\n" +
                "            </even:Requirements>\n" +
                "            <!--Optional:-->\n" +
                "            <even:AdditionalRequirements>\n" +
                "               <!--1 to 3 repetitions:-->\n" +
                "               <even:AdditionalRequirement>\n" +
                "                  <even:Code>?</even:Code>\n" +
                "                  <even:MainCategory>?</even:MainCategory>\n" +
                "                  <even:SubCategory>?</even:SubCategory>\n" +
                "                  <!--Optional:-->\n" +
                "                  <even:RequirementDetails>\n" +
                "                     <even:Length>?</even:Length>\n" +
                "                  </even:RequirementDetails>\n" +
                "               </even:AdditionalRequirement>\n" +
                "            </even:AdditionalRequirements>\n" +
                "            <!--Optional:-->\n" +
                "            <even:Custody>\n" +
                "               <!--Optional:-->\n" +
                "               <even:ReleaseDate>?</even:ReleaseDate>\n" +
                "               <!--Optional:-->\n" +
                "               <even:ReleaseType>?</even:ReleaseType>\n" +
                "               <!--Optional:-->\n" +
                "               <even:LicenceConditions>\n" +
                "                  <!--1 or more repetitions:-->\n" +
                "                  <even:LicenceCondition>\n" +
                "                     <even:Type>\n" +
                "                        <!--You have a CHOICE of the next 2 items at this level-->\n" +
                "                        <even:PreCJALicenceConditionType>\n" +
                "                           <even:Code>?</even:Code>\n" +
                "                           <even:MainCategory>?</even:MainCategory>\n" +
                "                           <even:SubCategory>?</even:SubCategory>\n" +
                "                        </even:PreCJALicenceConditionType>\n" +
                "                        <even:PostCJALicenceConditionType>\n" +
                "                           <even:Code>?</even:Code>\n" +
                "                           <even:MainCategory>?</even:MainCategory>\n" +
                "                           <even:SubCategory>?</even:SubCategory>\n" +
                "                        </even:PostCJALicenceConditionType>\n" +
                "                     </even:Type>\n" +
                "                  </even:LicenceCondition>\n" +
                "               </even:LicenceConditions>\n" +
                "            </even:Custody>\n" +
                "         </mes:Event>\n" +
                "      </mes:GetOffenderDetailsResponse>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>";

        final DeliusOffenderDetailsResponse expected = DeliusOffenderDetailsResponse
                .builder()
                .event(Event
                        .builder()
                        .additionalRequirements(ImmutableList.of(aRequirement()))
                        .commencementDate("?")
                        .court("?")
                        .courtType("?")
                        .custody(Custody
                                .builder()
                                .licenceConditions(ImmutableList.of(
                                        LicenceCondition
                                                .builder()
                                                .types(ImmutableList.of(Type
                                                        .builder()
                                                        .postCJALicenceConditionType(aLicenceCondition())
                                                        .preCJALicenceConditionType(aLicenceCondition())
                                                        .build()))
                                                .build()
                                ))
                                .releaseDate("?")
                                .releaseType("?")
                                .build())
                        .eventNumber("?")
                        .offenceCode("?")
                        .offenceDate("?")
                        .orderLength("?")
                        .orderType("?")
                        .requirements(ImmutableList.of(aRequirement()))
                        .uwHours("?")
                        .build())
                .offender(Offender
                        .builder()
                        .aliases(ImmutableList.of("?"))
                        .caseReferenceNumber("?")
                        .cro("?")
                        .dateOfBirth("?")
                        .ethnicity("?")
                        .forename1("?")
                        .forename2("?")
                        .forename3("?")
                        .gender("?")
                        .language("?")
                        .laoIndicator("?")
                        .lastName("?")
                        .mainAddress(anAddress())
                        .policeNationalComputerIdentifier("?")
                        .postcode("?")
                        .prisonNumber("?")
                        .religion("?")
                        .telephone("?")
                        .build())
                .build();

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        final SoapEnvelope soapEnvelope = xmlMapper.readValue(soap, SoapEnvelope.class);

        final DeliusOffenderDetailsResponse actual = soapEnvelope.getBody().getDeliusOffenderDetailsResponse();

        assertThat(actual).isEqualTo(expected);
    }

    private MainAddress anAddress() {
        return MainAddress
                .builder()
                .addressFirstLine(AddressFirstLine
                        .builder()
                        .buildingName("?")
                        .houseNumber("?")
                        .build())
                .county("?")
                .district("?")
                .streetName("?")
                .telephoneNumber("?")
                .townOrCity("?")
                .build();
    }

    public Category aRequirement() {
        return Category
                .builder()
                .code("?")
                .mainCategory("?")
                .requirementDetails(
                        RequirementDetails
                                .builder()
                                .length("?")
                                .build()
                )
                .subCategory("?")
                .build();
    }

    public Category aLicenceCondition() {
        return Category
                .builder()
                .mainCategory("?")
                .subCategory("?")
                .code("?")
                .build();
    }

}