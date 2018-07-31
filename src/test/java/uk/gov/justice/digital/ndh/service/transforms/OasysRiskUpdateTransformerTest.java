package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.assertj.core.api.Java6Assertions;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapBody;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapHeader;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.Risk;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.SubmitRiskDataResponseSoapBody;
import uk.gov.justice.digital.ndh.api.oasys.response.SubmitRiskDataResponseSoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OasysRiskUpdateTransformerTest {

    public static final String NOW = LocalDateTime.now().toString();

    @Test
    public void oasysRiskUpdateIsTransformedCorrectly() {

        final SoapEnvelope oasysRequest = anOasysRiskUpdate();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer();

        DeliusRiskUpdateSoapEnvelope expected = DeliusRiskUpdateSoapEnvelope
                .builder()
                .header(DeliusRiskUpdateSoapHeader
                        .builder()
                        .header(uk.gov.justice.digital.ndh.api.delius.request.Header
                                .builder()
                                .messageId(aCorrelationId())
                                .version("1.0")
                                .build())
                        .build())
                .body(DeliusRiskUpdateSoapBody
                        .builder()
                        .submitRiskDataRequest(uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest
                                .builder()
                                .risk(uk.gov.justice.digital.ndh.api.delius.request.RiskType
                                        .builder()
                                        .caseReferenceNumber("A1234")
                                        .riskOfHarm("riskOfHarm")
                                        .build())
                                .build())
                        .build())
                .build();

        DeliusRiskUpdateSoapEnvelope actual = transformer.deliusRiskUpdateRequestOf(oasysRequest);

        assertThat(actual).isEqualTo(expected);


    }

    private SoapEnvelope anOasysRiskUpdate() {
        return SoapEnvelope
                .builder()
                .header(SoapHeader.builder().build())
                .body(SoapBody
                        .builder()
                        .riskUpdateRequest(SubmitRiskDataRequest
                                .builder()
                                .cmsProbNumber("A1234")
                                .header(Header
                                        .builder()
                                        .applicationMode("appMode")
                                        .correlationID(aCorrelationId())
                                        .messageTimestamp(NOW)
                                        .oasysRUsername("oasysRUsername")
                                        .build())
                                .risk(Risk
                                        .builder()
                                        .RiskofHarm("riskOfHarm")
                                        .build())
                                .build())
                        .build()
                )
                .build();
    }

    private String aCorrelationId() {
        return "1234567890123456789012345678901";
    }


    @Test
    public void deliusRiskUpdateResponseIsTransformedCorrectly() {

        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.putObject("RiskUpdateResponse").put("CaseReferenceNumber", "A1234");


        DeliusRiskUpdateResponse deliusResponse = DeliusRiskUpdateResponse
                .builder()
                .body(root)
                .build();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer();

        SubmitRiskDataResponseSoapEnvelope expected = anOasysRiskUpdateResponse();

        final SubmitRiskDataResponseSoapEnvelope actual = transformer.oasysRiskUpdateResponseOf(deliusResponse, Optional.of(anOasysRiskUpdate()));

        assertThat(actual).isEqualTo(expected);
    }

    private SubmitRiskDataResponseSoapEnvelope anOasysRiskUpdateResponse() {
        return SubmitRiskDataResponseSoapEnvelope
                .builder()
                .body(SubmitRiskDataResponseSoapBody
                        .builder()
                        .response(RiskUpdateResponse
                                .builder()
                                .caseReferenceNumber("A1234")
                                .header(Header
                                        .builder()
                                        .applicationMode("appMode")
                                        .correlationID(aCorrelationId())
                                        .messageTimestamp(NOW)
                                        .oasysRUsername("oasysRUsername")
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    public void serializedDeliusRequestIsSchemaCompliant() throws JsonProcessingException {

        final XmlMapper xmlMapper = new XmlMapper();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer();

        final DeliusRiskUpdateSoapEnvelope builtMessage = transformer.deliusRiskUpdateRequestOf(anOasysRiskUpdate());

        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/common_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/Risk.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/SubmitRiskData/submit_risk_data_request.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        Java6Assertions.assertThat(result.isValid()).isTrue();

    }

    @Test
    public void serializedOasysResponseIsSchemaCompliant() throws JsonProcessingException {
        final XmlMapper xmlMapper = new XmlMapper();

        final SubmitRiskDataResponseSoapEnvelope builtMessage = anOasysRiskUpdateResponse();

        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/domain_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/riskudateresponse.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        Java6Assertions.assertThat(result.isValid()).isTrue();

    }

}