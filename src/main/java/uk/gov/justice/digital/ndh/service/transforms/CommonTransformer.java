package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.Header;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;

import java.io.IOException;
import java.util.Optional;

@Component
public class CommonTransformer {

    public static final String VERSION = "1.0";

    private final XmlMapper xmlMapper;
    private final ExceptionLogService exceptionLogService;

    @Autowired
    public CommonTransformer(XmlMapper xmlMapper, ExceptionLogService exceptionLogService) {
        this.xmlMapper = xmlMapper;
        this.exceptionLogService = exceptionLogService;
    }

    public Header headerOf(String correlationId) {
        return Header
                .builder()
                .messageId(correlationId)
                .version(VERSION)
                .build();
    }

    public SoapHeader deliusSoapHeaderOf(String correlationID) {
        return SoapHeader
                .builder()
                .header(headerOf(correlationID))
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


}
