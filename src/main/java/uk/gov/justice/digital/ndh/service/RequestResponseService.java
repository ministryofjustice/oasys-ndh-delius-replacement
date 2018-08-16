package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;

import java.util.Optional;

@Slf4j
public abstract class RequestResponseService {

    protected final CommonTransformer commonTransformer;
    protected final ExceptionLogService exceptionLogService;
    protected final MessageStoreService messageStoreService;
    protected final XmlMapper xmlMapper;

    public RequestResponseService(ExceptionLogService exceptionLogService, CommonTransformer commonTransformer, MessageStoreService messageStoreService, XmlMapper xmlMapper) {
        this.exceptionLogService = exceptionLogService;
        this.commonTransformer = commonTransformer;
        this.messageStoreService = messageStoreService;
        this.xmlMapper = xmlMapper;
    }

    protected Optional<String> stringXmlOf(Optional<SoapEnvelope> maybeTransformed, String correlationId, String offenderId, String processName) {
        return maybeTransformed.flatMap(transformed -> {
            try {
                final String transformedXml = xmlMapper.writeValueAsString(transformed);
                messageStoreService.writeMessage(transformedXml, correlationId, offenderId, processName, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation);
                return Optional.of(transformedXml);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(transformed.toString(), correlationId, "Can't serialize transformed risk update to XML: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

}
