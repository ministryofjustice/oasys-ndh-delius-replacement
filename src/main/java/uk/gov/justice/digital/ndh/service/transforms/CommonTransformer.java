package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import uk.gov.justice.digital.ndh.api.delius.request.Header;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommonTransformer {

    public static final String VERSION = "1.0";

    private final XmlMapper xmlMapper;
    private final ExceptionLogService exceptionLogService;
    public static final SAXReader READER = new SAXReader();

    @Autowired
    public CommonTransformer(XmlMapper xmlMapper, ExceptionLogService exceptionLogService) {
        this.xmlMapper = xmlMapper;
        this.exceptionLogService = exceptionLogService;
    }

    public Header deliusHeaderOf(String correlationId) {
        return Header
                .builder()
                .messageId(correlationId)
                .version(VERSION)
                .build();
    }

    public SoapHeader deliusSoapHeaderOf(String correlationID) {
        return SoapHeader
                .builder()
                .header(deliusHeaderOf(correlationID))
                .build();
    }

    public Optional<SoapEnvelope> asSoapEnvelope(String updateXml) {
        try {
            return Optional.of(xmlMapper.readValue(updateXml, SoapEnvelope.class));
        } catch (IOException e) {
            exceptionLogService.logFault(updateXml, null, "Can't asSoapEnvelope xml soap message from Oasys: " + e.getMessage());
        }
        return Optional.empty();
    }

    public String transformedResponseXmlOf(SoapEnvelope transformedResponse) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(transformedResponse);
    }


    public uk.gov.justice.digital.ndh.api.oasys.request.Header oasysHeaderOf(uk.gov.justice.digital.ndh.api.oasys.request.Header header) {
        return header.toBuilder().oasysRUsername("PCMS").build();
    }

    public String evaluateXpathText(String source, String xpath) throws DocumentException {
        org.dom4j.Document document = READER.read(new InputSource(new StringReader(source)));

        Optional<Node> maybeNode = Optional.ofNullable(document.selectSingleNode(xpath));

        return maybeNode.map(Node::getText).orElse(null);
    }

    public String deliusRiskFlagsOf(String riskFlags, Function<String, String> transform) {
        return Optional.ofNullable(riskFlags)
                .map(flags -> Arrays
                        .stream(flags.split(",", -1))
                        .map(transform)
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public String limitLength(String s, int i) {
        return s.substring(0, Math.min(s.length(), i));
    }
}
