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
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;
import uk.gov.justice.digital.ndh.service.transforms.OasysRiskUpdateTransformer;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class OasysRiskService extends RequestResponseService {

    private final OasysRiskUpdateTransformer oasysRiskUpdateTransformer;
    private final DeliusRiskUpdateClient deliusRiskUpdateClient;
    private final FaultTransformer faultTransformer;


    @Autowired
    public OasysRiskService(OasysRiskUpdateTransformer oasysRiskUpdateTransformer, CommonTransformer commonTransformer, XmlMapper xmlMapper, MessageStoreService messageStoreService, ExceptionLogService exceptionLogService, DeliusRiskUpdateClient deliusRiskUpdateClient, FaultTransformer faultTransformer) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper);
        this.oasysRiskUpdateTransformer = oasysRiskUpdateTransformer;
        this.deliusRiskUpdateClient = deliusRiskUpdateClient;
        this.faultTransformer = faultTransformer;
    }

    public Optional<String> processRiskUpdate(String updateXml) {

        val maybeOasysRiskUpdate = commonTransformer.asSoapEnvelope(updateXml);

        final String cmsProbNUmber = maybeOasysRiskUpdate.map(oru -> oru.getBody().getRiskUpdateRequest().getCmsProbNumber()).orElse(null);

        val correlationId = maybeOasysRiskUpdate.map(riskUpdate -> riskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID()).orElse(null);

        val maybeTransformed = deliusRiskUpdateRequestOf(updateXml, maybeOasysRiskUpdate);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId);

        val maybeRawResponse = rawDeliusUpdateRiskResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusRiskUpdateResponseOf(maybeRawResponse, correlationId);

        return maybeResponse.flatMap(response -> {
            try {
                try {
                    final String value = oasysRiskUpdateTransformer.stringResponseOf(response, maybeOasysRiskUpdate, maybeRawResponse);
                    return Optional.of(value);
                } catch (NDHMappingException ndhme) {
                    exceptionLogService.logMappingFail(
                            ndhme.getCode(),
                            ndhme.getSourceValue(),
                            ndhme.getSubject(),
                            correlationId,
                            cmsProbNUmber);

                    return mappingSoapFault(correlationId);
                }
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

    private Optional<String> mappingSoapFault(String correlationId) {
        return Optional.of(faultTransformer.mappingSoapFaultOf(correlationId));
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

    private Optional<SoapEnvelope> deliusRiskUpdateRequestOf(String updateXml, Optional<SoapEnvelope> maybeOasysRiskUpdate) {
        return maybeOasysRiskUpdate.map(oasysRiskUpdate -> {
            messageStoreService.writeMessage(updateXml,
                    oasysRiskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID(),
                    MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);
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
}
