package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.response.GetSubSetOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.delius.response.SubSetEvent;
import uk.gov.justice.digital.ndh.api.delius.response.SubSetOffender;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.InitialSearchResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookup;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookupRepository;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MappingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer.COURT_CODE_TYPE;
import static uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer.SENTENCE_CODE_TYPE;

public class OffenderTransformerTest {

    public static final CommonTransformer COMMON_TRANSFORMER = new CommonTransformer(new XmlMapper(), mock(ExceptionLogService.class));

    @Test
    public void canTransformOasysInitialSearchRequest() {
        final OffenderTransformer offenderTransformer = new OffenderTransformer(COMMON_TRANSFORMER, mock(MappingService.class), mock(RequirementLookupRepository.class));

        SoapEnvelope oasysRequest = anOasysInitialSearchRequest();

        SoapEnvelope expected = SoapEnvelope
                .builder()
                .header(COMMON_TRANSFORMER.deliusSoapHeaderOf("1234567890123456789012345678901"))
                .body(SoapBody
                        .builder()
                        .getSubSetOffenderEventRequest(
                                GetSubSetOffenderEventRequest
                                        .builder()
                                        .surname("Jones")
                                        .forename1("forename1")
                                        .caseReferenceNumber("XYZ123")
                                        .build())
                        .build())
                .build();

        SoapEnvelope actual = offenderTransformer.deliusInitialSearchRequestOf(oasysRequest);

        assertThat(actual).isEqualTo(expected);
    }

    private SoapEnvelope anOasysInitialSearchRequest() {
        return SoapEnvelope
                .builder()
                .body(SoapBody
                        .builder()
                        .initialSearchRequest(InitialSearchRequest
                                .builder()
                                .cmsProbNumber("XYZ123")
                                .familyName("Jones")
                                .forename1("forename1")
                                .header(Header
                                        .builder()
                                        .oasysRUsername("oasysRUsername")
                                        .messageTimestamp(LocalDateTime.now().toString())
                                        .correlationID("1234567890123456789012345678901")
                                        .applicationMode("applicationMode")
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    public void serializedDeliusRequestIsSchemaCompliant() throws JsonProcessingException {

        final XmlMapper xmlMapper = getXmlMapper();
        final OffenderTransformer transformer = new OffenderTransformer(COMMON_TRANSFORMER, mock(MappingService.class), mock(RequirementLookupRepository.class));

        final SoapEnvelope builtMessage = transformer.deliusInitialSearchRequestOf(anOasysInitialSearchRequest());

        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/common_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/GetSubSetOffenderDetails/get_subset_offender_event_request.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        assertThat(result.isValid()).isTrue();

    }

    private XmlMapper getXmlMapper() {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return xmlMapper;
    }

    @Test
    public void canTransformDeliusInitialSearchResponse() {
        final MappingService mappingService = mock(MappingService.class);

        when(mappingService.descriptionOf(eq("orderType"), eq(SENTENCE_CODE_TYPE))).thenReturn("sentenceCode");

        final OffenderTransformer transformer = new OffenderTransformer(COMMON_TRANSFORMER, mappingService, mock(RequirementLookupRepository.class));

        final SoapEnvelope deliusResponse = aDeliusInitialSearchResponse(COMMON_TRANSFORMER);
        final SoapEnvelope oasysRequest = anOasysInitialSearchRequest();

        final SoapEnvelope expected = anOasysInitialSearchResponse(oasysRequest);

        Optional<SoapEnvelope> actual = transformer.initialSearchResponseTransform.apply(Optional.ofNullable(oasysRequest), Optional.of(deliusResponse));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    public SoapEnvelope anOasysInitialSearchResponse(SoapEnvelope oasysRequest) {
        return SoapEnvelope
                .builder()
                .body(SoapBody
                        .builder()
                        .initialSearchResponse(InitialSearchResponse
                                .builder()
                                .header(oasysRequest.getBody().getInitialSearchRequest().getHeader().toBuilder().oasysRUsername("PCMS").build())
                                .subSetOffenders(ImmutableList.of(
                                        uk.gov.justice.digital.ndh.api.oasys.response.SubSetOffender
                                                .builder()
                                                .laoIndicator("laoIndicator")
                                                .gender("1")
                                                .forename1("forename1")
                                                .familyName("Jones")
                                                .dateOfBirth("1970-01-01")
                                                .cmsProbNumber("XYZ123")
                                                .subSetEvents(ImmutableList.of(
                                                        uk.gov.justice.digital.ndh.api.oasys.response.SubSetEvent
                                                                .builder()
                                                                .sentenceCode("sentenceCode")
                                                                .eventNumber("1")
                                                                .sentenceDate("1972-02-02")
                                                                .build(),
                                                        uk.gov.justice.digital.ndh.api.oasys.response.SubSetEvent
                                                                .builder()
                                                                .sentenceCode("sentenceCode")
                                                                .eventNumber("2")
                                                                .sentenceDate("1972-02-02")
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build())
                        .build())
                .build();
    }

    private SoapEnvelope aDeliusInitialSearchResponse(CommonTransformer commonTransformer) {
        return SoapEnvelope
                .builder()
                .header(commonTransformer.deliusSoapHeaderOf("1234567890123456789012345678901"))
                .body(SoapBody
                        .builder()
                        .getSubSetOffenderDetailsResponse(
                                GetSubSetOffenderDetailsResponse
                                        .builder()
                                        .subSetOffender(
                                                SubSetOffender
                                                        .builder()
                                                        .caseReferenceNumber("XYZ123")
                                                        .dateOfBirth("1970-01-01")
                                                        .forename1("forename1")
                                                        .gender("M")
                                                        .laoIndicator("laoIndicator")
                                                        .lastName("Jones")
                                                        .build())
                                        .subSetEvents(ImmutableList.of(
                                                aSubSetEvent("1"),
                                                aSubSetEvent("2")
                                        ))
                                        .build()
                        )
                        .build())
                .build();
    }

    private SubSetEvent aSubSetEvent(String eventNumber) {
        return SubSetEvent
                .builder()
                .commencementDate("1972-02-02")
                .eventNumber(eventNumber)
                .orderType("orderType")
                .build();
    }

    @Test
    public void serializedOasysInitialSearchResponseIsSchemaCompliant() throws JsonProcessingException {
        final XmlMapper xmlMapper = getXmlMapper();

        final SoapEnvelope builtMessage = anOasysInitialSearchResponse(anOasysInitialSearchRequest());

        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/domain_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/subsetEvent.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/subsetOffender.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/initialSearchResponse.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void offenderDetailsResponseIsTransformedAndSerializesCorrectly() throws IOException {

        final String dummyOasysRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:off=\"http://www.hp.com/NDH_Web_Service/OffenderDetailsRequest\" xmlns:dom=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">\n" +
                "   <soap:Header/>\n" +
                "   <soap:Body>\n" +
                "      <off:OffenderDetailsRequest>\n" +
                "         <off:header>\n" +
                "            <dom:ApplicationMode>I</dom:ApplicationMode>\n" +
                "            <dom:CorrelationID>OASYSRPCWWS20180910130951604609</dom:CorrelationID>\n" +
                "            <dom:OASysRUsername>CENTRALSUPPORTONE</dom:OASysRUsername>\n" +
                "            <dom:MessageTimestamp>2018-09-10T13:59:51+01:00</dom:MessageTimestamp>\n" +
                "         </off:header>\n" +
                "      </off:OffenderDetailsRequest>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>";

        final String deliusResponse = "<soap:Envelope\n" +
                "\txmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                "\txmlns:com=\"http://www.bconline.co.uk/oasys/common\"\n" +
                "\txmlns:mes=\"http://www.bconline.co.uk/oasys/messages\"\n" +
                "\txmlns:off=\"http://www.bconline.co.uk/oasys/offender\"\n" +
                "\txmlns:even=\"http://www.bconline.co.uk/oasys/event\">\n" +
                "\t<soap:Header>\n" +
                "\t\t<com:Header>\n" +
                "\t\t\t<com:Version>?</com:Version>\n" +
                "\t\t\t<com:MessageID>?</com:MessageID>\n" +
                "\t\t</com:Header>\n" +
                "\t</soap:Header>\n" +
                "\t<soap:Body>\n" +
                "\t\t<ns4:GetOffenderDetailsResponse\n" +
                "\t\t\txmlns:ns4=\"http://www.bconline.co.uk/oasys/messages\"\n" +
                "\t\t\txmlns:ns2=\"http://www.bconline.co.uk/oasys/event\"\n" +
                "\t\t\txmlns:ns3=\"http://www.bconline.co.uk/oasys/fault\"\n" +
                "\t\t\txmlns:ns5=\"http://www.bconline.co.uk/oasys/offender\"\n" +
                "\t\t\txmlns:ns6=\"http://www.bconline.co.uk/oasys/assessment\"\n" +
                "\t\t\txmlns:ns7=\"http://www.bconline.co.uk/oasys/risk\">\n" +
                "\t\t\t<ns4:Offender>\n" +
                "\t\t\t\t<ns5:LAOIndicator>N</ns5:LAOIndicator>\n" +
                "\t\t\t\t<ns5:CaseReferenceNumber>H923526</ns5:CaseReferenceNumber>\n" +
                "\t\t\t\t<ns5:LastName>ServiceReleaseTwelveSurThree</ns5:LastName>\n" +
                "\t\t\t\t<ns5:Forename1>ServiceReleaseTwelveForThree</ns5:Forename1>\n" +
                "\t\t\t\t<ns5:Gender>M</ns5:Gender>\n" +
                "\t\t\t\t<ns5:DateOfBirth>1982-07-19</ns5:DateOfBirth>\n" +
                "\t\t\t\t<ns5:Telephone />\n" +
                "\t\t\t</ns4:Offender>\n" +
                "\t\t\t<ns4:Event>\n" +
                "\t\t\t\t<ns2:EventNumber>1</ns2:EventNumber>\n" +
                "\t\t\t\t<ns2:OffenceCode>03700</ns2:OffenceCode>\n" +
                "\t\t\t\t<ns2:OffenceDate>2017-07-19</ns2:OffenceDate>\n" +
                "\t\t\t\t<ns2:CommencementDate>2017-07-19</ns2:CommencementDate>\n" +
                "\t\t\t\t<ns2:OrderType>201</ns2:OrderType>\n" +
                "\t\t\t\t<ns2:OrderLength>14</ns2:OrderLength>\n" +
                "\t\t\t\t<ns2:Court>THMSMC</ns2:Court>\n" +
                "\t\t\t\t<ns2:CourtType>MAG</ns2:CourtType>\n" +
                "\t\t\t\t<ns2:Requirements>\n" +
                "\t\t\t\t\t<ns2:Requirement>\n" +
                "\t\t\t\t\t\t<ns2:Code />\n" +
                "\t\t\t\t\t\t<ns2:MainCategory>X</ns2:MainCategory>\n" +
                "\t\t\t\t\t\t<ns2:SubCategory>X02</ns2:SubCategory>\n" +
                "\t\t\t\t\t\t<ns2:RequirementDetails>\n" +
                "\t\t\t\t\t\t\t<ns2:Length>12</ns2:Length>\n" +
                "\t\t\t\t\t\t</ns2:RequirementDetails>\n" +
                "\t\t\t\t\t</ns2:Requirement>\n" +
                "\t\t\t\t</ns2:Requirements>\n" +
                "\t\t\t</ns4:Event>\n" +
                "\t\t</ns4:GetOffenderDetailsResponse>\n" +
                "\t</soap:Body>\n" +
                "</soap:Envelope>";

        final String expected = "<soap:Envelope\n" +
                "\txmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                "\txmlns:off=\"http://www.hp.com/NDH_Web_Service/Offender_Details_Response\"\n" +
                "\txmlns:dom=\"http://www.hp.com/NDH_Web_Service/DomainTypes\"\n" +
                "\txmlns:off1=\"http://www.hp.com/NDH_Web_Service/offender\"\n" +
                "\txmlns:even=\"http://www.hp.com/NDH_Web_Service/event\">\n" +
                "\t<soap:Header/>\n" +
                "\t<soap:Body>\n" +
                "\t\t<ns0:OffenderDetailsResponse\n" +
                "\t\t\txmlns:ns0=\"http://www.hp.com/NDH_Web_Service/Offender_Details_Response\">\n" +
                "\t\t\t<ns0:Header>\n" +
                "\t\t\t\t<ns1:ApplicationMode\n" +
                "\t\t\t\t\txmlns:ns1=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">I\n" +
                "\t\t\t\t</ns1:ApplicationMode>\n" +
                "\t\t\t\t<ns1:CorrelationID\n" +
                "\t\t\t\t\txmlns:ns1=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">OASYSRPCWWS20180910130951604609\n" +
                "\t\t\t\t</ns1:CorrelationID>\n" +
                "\t\t\t\t<ns1:OASysRUsername\n" +
                "\t\t\t\t\txmlns:ns1=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">CENTRALSUPPORTONE\n" +
                "\t\t\t\t</ns1:OASysRUsername>\n" +
                "\t\t\t\t<ns1:MessageTimestamp\n" +
                "\t\t\t\t\txmlns:ns1=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">2018-09-10T13:59:51+01:00\n" +
                "\t\t\t\t</ns1:MessageTimestamp>\n" +
                "\t\t\t</ns0:Header>\n" +
                "\t\t\t<ns1:OffenderDetail\n" +
                "\t\t\t\txmlns:ns1=\"http://www.hp.com/NDH_Web_Service/offender\">\n" +
                "\t\t\t\t<ns1:CMSProbNumber>H923526</ns1:CMSProbNumber>\n" +
                "\t\t\t\t<ns1:FamilyName>ServiceReleaseTwelveSurThree</ns1:FamilyName>\n" +
                "\t\t\t\t<ns1:Forename1>ServiceReleaseTwelveForThree</ns1:Forename1>\n" +
                "\t\t\t\t<ns1:Gender>1</ns1:Gender>\n" +
                "\t\t\t\t<ns1:DateOfBirth>1982-07-19</ns1:DateOfBirth>\n" +
                "\t\t\t\t<ns1:EthnicCategory />\n" +
                "\t\t\t\t<ns1:TelephoneNumber />\n" +
                "\t\t\t\t<ns1:Language />\n" +
                "\t\t\t\t<ns1:LAOIndicator>N</ns1:LAOIndicator>\n" +
                "\t\t\t</ns1:OffenderDetail>\n" +
                "\t\t\t<ns1:EventDetail\n" +
                "\t\t\t\txmlns:ns1=\"http://www.hp.com/NDH_Web_Service/event\">\n" +
                "\t\t\t\t<ns1:EventNumber>1</ns1:EventNumber>\n" +
                "\t\t\t\t<ns1:Offences>\n" +
                "\t\t\t\t\t<ns2:OffenceGroupCode\n" +
                "\t\t\t\t\t\txmlns:ns2=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">037\n" +
                "\t\t\t\t\t</ns2:OffenceGroupCode>\n" +
                "\t\t\t\t\t<ns2:OffenceSubCode\n" +
                "\t\t\t\t\t\txmlns:ns2=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">00\n" +
                "\t\t\t\t\t</ns2:OffenceSubCode>\n" +
                "\t\t\t\t</ns1:Offences>\n" +
                "\t\t\t\t<ns1:SentenceCode>910</ns1:SentenceCode>\n" +
                "\t\t\t\t<ns1:SentenceDate>2017-07-19</ns1:SentenceDate>\n" +
                "\t\t\t\t<ns1:OffenceDate>2017-07-19</ns1:OffenceDate>\n" +
                "\t\t\t\t<ns1:SentenceLength>14</ns1:SentenceLength>\n" +
                "\t\t\t\t<ns1:CourtCode>THMSMC</ns1:CourtCode>\n" +
                "\t\t\t\t<ns1:CourtType>MC</ns1:CourtType>\n" +
                "\t\t\t\t<ns1:SentenceDetail>\n" +
                "\t\t\t\t\t<ns1:AttributeCategory>CJA_REQUIREMENT</ns1:AttributeCategory>\n" +
                "\t\t\t\t\t<ns1:AttributeElement>EXCLUSION</ns1:AttributeElement>\n" +
                "\t\t\t\t\t<ns1:Description>Named Licenced Premises</ns1:Description>\n" +
                "\t\t\t\t</ns1:SentenceDetail>\n" +
                "\t\t\t</ns1:EventDetail>\n" +
                "\t\t</ns0:OffenderDetailsResponse>\n" +
                "\t</soap:Body>\n" +
                "</soap:Envelope>";

        final XmlMapper xmlMapper = getXmlMapper();

        final SoapEnvelope dummyOasysRequesSoapEnvelope = xmlMapper.readValue(dummyOasysRequest, SoapEnvelope.class);
        final SoapEnvelope deliusResponseSoapEnvelope = xmlMapper.readValue(deliusResponse, SoapEnvelope.class);

        final MappingService mappingService = mock(MappingService.class);
        final RequirementLookupRepository requirementLookupRepository = mock(RequirementLookupRepository.class);

        when(mappingService.sourceValueOf("MAG", COURT_CODE_TYPE)).thenReturn("MC");
        when(mappingService.sourceValueOf("201", SENTENCE_CODE_TYPE)).thenReturn("910");
        when(requirementLookupRepository.findByReqTypeAndReqCodeAndSubCode("N", "X","X02")).thenReturn(
                Optional.of(RequirementLookup.builder()
                    .activityDesc("Named Licenced Premises")
                    .reqType("N")
                .reqCode("X")
                .subCode("X02")
                .sentenceAttributeCat("CJA_REQUIREMENT")
                .sentenceAttributeElm("EXCLUSION")
                .build()));

        OffenderTransformer offenderTransformer = new OffenderTransformer(new CommonTransformer(xmlMapper, mock(ExceptionLogService.class)), mappingService, requirementLookupRepository);

        final Optional<SoapEnvelope> oasysResponseSoapEnvelope = offenderTransformer.offenderDetailsResponseTransform.apply(Optional.of(dummyOasysRequesSoapEnvelope), Optional.of(deliusResponseSoapEnvelope));

        final String oasysResponseXml = xmlMapper.writeValueAsString(oasysResponseSoapEnvelope.get());

        Diff myDiff = DiffBuilder.compare(oasysResponseXml).withTest(expected)
                .withDifferenceEvaluator(DifferenceEvaluators.Default)
                .ignoreComments()
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertThat(myDiff.hasDifferences()).isFalse();

    }


}