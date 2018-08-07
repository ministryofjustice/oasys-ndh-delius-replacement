package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.transforms.OasysRiskUpdateTransformer;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class OasysRiskService {

    private final OasysRiskUpdateTransformer oasysRiskUpdateTransformer;
    private final XmlMapper xmlMapper;
    private final MessageStoreService messageStoreService;
    private final ExceptionLogService exceptionLogService;
    private final DeliusRiskUpdateClient deliusRiskUpdateClient;


    @Autowired
    public OasysRiskService(OasysRiskUpdateTransformer oasysRiskUpdateTransformer, XmlMapper xmlMapper, MessageStoreService messageStoreService, ExceptionLogService exceptionLogService, DeliusRiskUpdateClient deliusRiskUpdateClient) {
        this.oasysRiskUpdateTransformer = oasysRiskUpdateTransformer;
        this.xmlMapper = xmlMapper;
        this.messageStoreService = messageStoreService;
        this.exceptionLogService = exceptionLogService;
        this.deliusRiskUpdateClient = deliusRiskUpdateClient;
    }

    public Optional<String> processRiskUpdate(String updateXml) {

        val maybeOasysRiskUpdate = read(updateXml);

        val correlationId = maybeOasysRiskUpdate.map(riskUpdate -> riskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID()).orElse(null);

        val maybeTransformed = deliusRiskUpdateRequestOf(updateXml, maybeOasysRiskUpdate);

        val maybeTransformedXml = deliusRiskUpdateXmlOf(maybeTransformed, correlationId);

        val maybeRawResponse = rawDeliusUpdateRiskResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusRiskUpdateResponseOf(maybeRawResponse, correlationId);

        return maybeResponse.flatMap(response -> {
            try {
                return Optional.of(oasysRiskUpdateTransformer.stringResponseOf(response, maybeOasysRiskUpdate, maybeRawResponse));
            } catch (DocumentException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(response.toString(), correlationId, "Can't transform fault response: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(response.toString(), correlationId, "Can't serialize transformed risk update response: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private Optional<DeliusRiskUpdateResponse> deliusRiskUpdateResponseOf(Optional<String> maybeRawResponse, String correlationId) {
        return maybeRawResponse.flatMap(rawResponse -> {
            messageStoreService.writeMessage(rawResponse, correlationId, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation);
            try {
                return Optional.of(xmlMapper.readValue(rawResponse, DeliusRiskUpdateResponse.class));
            } catch (IOException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(rawResponse, correlationId, "Can't deserialize delius risk update response: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<String> rawDeliusUpdateRiskResponseOf(Optional<String> maybeTransformedXml, String correlationId) {
        return maybeTransformedXml.flatMap((String transformedXml) -> callDeliusRiskUpdate(transformedXml, correlationId));
    }

    private Optional<String> deliusRiskUpdateXmlOf(Optional<SoapEnvelope> maybeTransformed, String correlationId) {
        return maybeTransformed.flatMap(transformed -> {
            try {
                final String transformedXml = xmlMapper.writeValueAsString(transformed);
                messageStoreService.writeMessage(transformedXml, correlationId, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation);
                return Optional.of(transformedXml);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(transformed.toString(), correlationId, "Can't serialize transformed risk update to XML: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<SoapEnvelope> deliusRiskUpdateRequestOf(String updateXml, Optional<SoapEnvelope> maybeOasysRiskUpdate) {
        return maybeOasysRiskUpdate.map(oasysRiskUpdate -> {
            messageStoreService.writeMessage(updateXml, maybeOasysRiskUpdate.get().getHeader().getCorrelationId(), MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);
            return oasysRiskUpdateTransformer.deliusRiskUpdateRequestOf(oasysRiskUpdate);
        });
    }

    private Optional<String> callDeliusRiskUpdate(String transformedXml, String correlationId) {
        try {
            return Optional.of(deliusRiskUpdateClient.deliusWebServiceResponseOf(transformedXml));
        } catch (UnirestException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(transformedXml, correlationId, "Can't talk to Delius risk update endpoint: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<SoapEnvelope> read(String updateXml) {
        try {
            return Optional.of(xmlMapper.readValue(updateXml, SoapEnvelope.class));
        } catch (IOException e) {
            exceptionLogService.logFault(updateXml, null, "Can't read xml soap message from Oasys: " + e.getMessage());
        }
        return Optional.empty();
    }
}
