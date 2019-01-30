package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;

import java.util.Optional;

@Slf4j
public abstract class RequestResponseService {

    protected final CommonTransformer commonTransformer;
    protected final ExceptionLogService exceptionLogService;
    protected final MessageStoreService messageStoreService;
    protected final XmlMapper xmlMapper;
    protected final FaultTransformer faultTransformer;

    public RequestResponseService(ExceptionLogService exceptionLogService, CommonTransformer commonTransformer, MessageStoreService messageStoreService, XmlMapper xmlMapper, FaultTransformer faultTransformer) {
        this.exceptionLogService = exceptionLogService;
        this.commonTransformer = commonTransformer;
        this.messageStoreService = messageStoreService;
        this.xmlMapper = xmlMapper;
        this.faultTransformer = faultTransformer;
    }

    protected Optional<String> stringXmlOf(Optional<SoapEnvelopeSpec1_2> maybeTransformed, String correlationId) {
        return maybeTransformed.flatMap(transformed -> {
            try {
                final String transformedXml = xmlMapper.writeValueAsString(transformed);
                return Optional.of(transformedXml);
            } catch (JsonProcessingException e) {
                log.error("SOAP fail: {} {}", e.getMessage(), transformed);
                exceptionLogService.logFault(transformed.toString(), correlationId, "Can't serialize transformed risk update to XML: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    public Optional<String> handleSoapFault(String correlationId, Optional<String> maybeRawResponse, String body) {
        log.error("SOAP fail: {}", body);

        exceptionLogService.logFault(maybeRawResponse.get(), correlationId, "SOAP Fault returned from Delius initialSearch service");
        try {
            return Optional.ofNullable(faultTransformer.oasysFaultResponseOf(maybeRawResponse.get(), correlationId));
        } catch (DocumentException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(body, correlationId, "Can't serialize SOAP Fault response: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String handleOkResponse(String correlationId, SoapEnvelopeSpec1_2 oasysResponse) {
        try {
            return commonTransformer.asString(oasysResponse);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(oasysResponse.toString(), correlationId, "Can't serialize transformed risk update response: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
