package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderTransformerTest {

    @Test
    public void canTransformOasysInitialSearchRequest() {
        final CommonTransformer commonTransformer = new CommonTransformer();
        OffenderTransformer offenderTransformer = new OffenderTransformer(commonTransformer);

        SoapEnvelope oasysInitialSearch = anOasysInitialSearch();

        SoapEnvelope expected = SoapEnvelope
                .builder()
                .header(commonTransformer.deliusSoapHeaderOf("correlationId"))
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

        SoapEnvelope actual = offenderTransformer.deliusInitialSearchRequestOf(oasysInitialSearch);

        assertThat(actual).isEqualTo(expected);
    }

    private SoapEnvelope anOasysInitialSearch() {
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
                                        .messageTimestamp("messageTimestamp")
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

        OffenderTransformer transformer = new OffenderTransformer(new CommonTransformer());

        final SoapEnvelope builtMessage = transformer.deliusInitialSearchRequestOf(anOasysInitialSearch());

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


}