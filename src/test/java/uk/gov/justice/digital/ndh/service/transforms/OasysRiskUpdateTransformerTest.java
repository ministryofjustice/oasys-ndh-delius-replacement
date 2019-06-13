package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.Risk;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OasysRiskUpdateTransformerTest {

    private static final String NOW = LocalDateTime.now().toString();

    @Test
    public void riskFlagsTransformedCorrectly() {
        CommonTransformer transformer = new CommonTransformer(mock(XmlMapper.class), mock(ObjectMapper.class), mock(ExceptionLogService.class));

        assertThat(transformer.deliusRiskFlagsOf(",,,,,", OasysRiskUpdateTransformer.deliusRiskFlagOf)).isEqualTo("N,N,N,N,N,N");
        assertThat(transformer.deliusRiskFlagsOf(",M,H", OasysRiskUpdateTransformer.deliusRiskFlagOf)).isEqualTo("N,M,H");

    }

    @Test
    public void oasysRiskUpdateIsTransformedCorrectly() {

        final SoapEnvelopeSpec1_2 oasysRequest = anOasysRiskUpdate();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer(new CommonTransformer(getXmlMapper(), mock(ObjectMapper.class), mock(ExceptionLogService.class)));

        SoapEnvelopeSpec1_2 expected = SoapEnvelopeSpec1_2
                .builder()
                .header(SoapHeader
                        .builder()
                        .header(uk.gov.justice.digital.ndh.api.delius.request.Header
                                .builder()
                                .messageId(aCorrelationId())
                                .version("1.0")
                                .build())
                        .build())
                .body(SoapBody
                        .builder()
                        .submitRiskDataRequest(uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest
                                .builder()
                                .risk(uk.gov.justice.digital.ndh.api.delius.request.RiskType
                                        .builder()
                                        .caseReferenceNumber("A1234")
                                        .riskOfHarm("N,M,H")
                                        .build())
                                .build())
                        .build())
                .build();

        SoapEnvelopeSpec1_2 actual = transformer.deliusRiskUpdateRequestOf(oasysRequest);

        assertThat(actual).isEqualTo(expected);


    }

    private SoapEnvelopeSpec1_2 anOasysRiskUpdate() {
        return SoapEnvelopeSpec1_2
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
                                .riskFlags(",M,H")
                                .build())
                        .build()
                )
                .build();
    }

    private String aCorrelationId() {
        return "OASYSRPCWWS20180910130951604609";
    }


    @Test
    public void deliusRiskUpdateResponseIsTransformedCorrectly() {

        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.putObject("SubmitRiskDataResponse").put("CaseReferenceNumber", "A1234");


        DeliusRiskUpdateResponse deliusResponse = DeliusRiskUpdateResponse
                .builder()
                .body(root)
                .build();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer(new CommonTransformer(getXmlMapper(), mock(ObjectMapper.class), mock(ExceptionLogService.class)));

        SoapEnvelopeSpec1_2 expected = anOasysRiskUpdateResponse();

        final Optional<SoapEnvelopeSpec1_2> actual = transformer.oasysRiskUpdateResponseOf(Optional.of(deliusResponse), Optional.of(anOasysRiskUpdate()));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    private SoapEnvelopeSpec1_2 anOasysRiskUpdateResponse() {
        return SoapEnvelopeSpec1_2
                .builder()
                .body(SoapBody
                        .builder()
                        .riskUpdateResponse(RiskUpdateResponse
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

        final XmlMapper xmlMapper = getXmlMapper();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer(new CommonTransformer(xmlMapper, mock(ObjectMapper.class), mock(ExceptionLogService.class)));

        final SoapEnvelopeSpec1_2 builtMessage = transformer.deliusRiskUpdateRequestOf(anOasysRiskUpdate());

        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/common_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/Risk.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/SubmitRiskData/submit_risk_data_request.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        assertThat(result.isValid()).isTrue();

    }

    @Test
    public void serializedOasysResponseIsSchemaCompliant() throws JsonProcessingException {
        final XmlMapper xmlMapper = getXmlMapper();

        final SoapEnvelopeSpec1_2 builtMessage = anOasysRiskUpdateResponse();

        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/domain_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("OasysToNDH wsdls/xsd/riskudateresponse.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        assertThat(result.isValid()).isTrue();
    }

    private XmlMapper getXmlMapper() {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return xmlMapper;
    }


    @Test
    public void faultResponseIsTransformedCorrectly() throws DocumentException {

        final String deliusFaultResponseXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/realFaultResponseFromDelius.xml")))
                .lines().collect(Collectors.joining("\n"));

        final FaultTransformer faultTransformer = new FaultTransformer();
        final String actual = faultTransformer.oasysFaultResponseOf(deliusFaultResponseXml, aCorrelationId());

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<S:Envelope\n" +
                "\txmlns:S=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "\t<S:Body>\n" +
                "\t\t<S:Fault>\n" +
                "\t\t\t<S:Code>\n" +
                "\t\t\t\t<S:Value>S:NDH</S:Value>\n" +
                "\t\t\t</S:Code>\n" +
                "\t\t\t<S:Reason>\n" +
                "\t\t\t\t<S:Text xml:lang=\"en\">S:PCMS Web Service has returned an error</S:Text>\n" +
                "\t\t\t</S:Reason>\n" +
                "\t\t\t<S:Detail>\n" +
                "\t\t\t\t<ndh:ValidationFailureException\n" +
                "\t\t\t\t\txmlns:ndh=\"http://www.hp.com/NDH_Web_Service/Fault\">\n" +
                "\t\t\t\t\t<ndh:Code>OASYSERR003</ndh:Code>\n" +
                "\t\t\t\t\t<ndh:Description>Input Data Failed XML Schema Validation</ndh:Description>\n" +
                "\t\t\t\t\t<ndh:Timestamp>2018-08-01T12:06:26.075+01:00</ndh:Timestamp>\n" +
                "\t\t\t\t\t<ndh:RequestMessage>1234567890123456789012345678901</ndh:RequestMessage>\n" +
                "\t\t\t\t</ndh:ValidationFailureException>\n" +
                "\t\t\t</S:Detail>\n" +
                "\t\t</S:Fault>\n" +
                "\t</S:Body>\n" +
                "</S:Envelope>";


        Diff myDiff = DiffBuilder.compare(Input.fromString(expected)).withTest(Input.fromString(actual))
                .checkForIdentical()
                .ignoreWhitespace()
                .build();

        assertThat(myDiff.hasDifferences()).isFalse();
    }


}