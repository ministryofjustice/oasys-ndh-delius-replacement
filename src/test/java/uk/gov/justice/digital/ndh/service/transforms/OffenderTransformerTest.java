package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.xmlunit.builder.Input;
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
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MappingService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OffenderTransformerTest {

    public static final CommonTransformer COMMON_TRANSFORMER = new CommonTransformer(new XmlMapper(), mock(ExceptionLogService.class));

    @Test
    public void canTransformOasysInitialSearchRequest() {
        final OffenderTransformer offenderTransformer = new OffenderTransformer(COMMON_TRANSFORMER, mock(FaultTransformer.class), mock(MappingService.class));

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
        final OffenderTransformer transformer = new OffenderTransformer(COMMON_TRANSFORMER, mock(FaultTransformer.class), mock(MappingService.class));

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

        when(mappingService.descriptionOf(eq("orderType"), eq(OffenderTransformer.SENTENCE_CODE_TYPE))).thenReturn("sentenceCode");

        final OffenderTransformer transformer = new OffenderTransformer(COMMON_TRANSFORMER, mock(FaultTransformer.class), mappingService);

        final SoapEnvelope deliusResponse = aDeliusInitialSearchResponse(COMMON_TRANSFORMER);
        final SoapEnvelope oasysRequest = anOasysInitialSearchRequest();

        final SoapEnvelope expected = anOasysInitialSearchResponse(oasysRequest);

        SoapEnvelope actual = transformer.oasysInitialSearchResponseOf(deliusResponse, java.util.Optional.ofNullable(oasysRequest));

        assertThat(actual).isEqualTo(expected);
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
    public void serializedOasysResponseIsSchemaCompliant() throws JsonProcessingException {
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



}