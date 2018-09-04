package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public static final String NDH_WEB_SERVICE_RISK_UPDATE = "NDH_Web_Service_Risk_Update";
    private final OasysRiskUpdateTransformer oasysRiskUpdateTransformer;
    private final DeliusSOAPClient deliusRiskUpdateClient;
    private final FaultTransformer faultTransformer;


    @Autowired
    public OasysRiskService(OasysRiskUpdateTransformer oasysRiskUpdateTransformer,
                            CommonTransformer commonTransformer,
                            XmlMapper xmlMapper,
                            MessageStoreService messageStoreService,
                            ExceptionLogService exceptionLogService,
                            @Qualifier("riskUpdateClient") DeliusSOAPClient deliusRiskUpdateClient,
                            FaultTransformer faultTransformer) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper);
        this.oasysRiskUpdateTransformer = oasysRiskUpdateTransformer;
        this.deliusRiskUpdateClient = deliusRiskUpdateClient;
        this.faultTransformer = faultTransformer;
    }

    public Optional<String> processRiskUpdate(String updateXml) {

        val maybeOasysRiskUpdate = commonTransformer.asSoapEnvelope(updateXml);

        val cmsProbNUmber = maybeOasysRiskUpdate.map(oru -> oru.getBody().getRiskUpdateRequest().getCmsProbNumber()).orElse(null);

        val correlationId = maybeOasysRiskUpdate.map(riskUpdate -> riskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID()).orElse(null);

        val maybeTransformed = deliusRiskUpdateRequestOf(updateXml, maybeOasysRiskUpdate, correlationId, cmsProbNUmber);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId, cmsProbNUmber, NDH_WEB_SERVICE_RISK_UPDATE);

        val maybeRawResponse = rawDeliusUpdateRiskResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusRiskUpdateResponseOf(maybeRawResponse, correlationId, cmsProbNUmber);

        return maybeResponse.flatMap(response -> {
            try {
                try {
                    final String value = stringResponseOf(response, maybeOasysRiskUpdate, maybeRawResponse, correlationId, cmsProbNUmber);
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

    public String stringResponseOf(DeliusRiskUpdateResponse response, Optional<SoapEnvelope> maybeOasysRiskUpdate, Optional<String> rawDeliusResponse, String correlationId, String offenderId) throws DocumentException, JsonProcessingException {
        if (response.isSoapFault()) {
            exceptionLogService.logFault(rawDeliusResponse.get(), correlationId, "SOAP Fault returned from Delius riskUpdate service");
            return faultTransformer.oasysFaultResponseOf(rawDeliusResponse.get(), correlationId);
        } else {
            messageStoreService.writeMessage(rawDeliusResponse.get(), correlationId, offenderId, NDH_WEB_SERVICE_RISK_UPDATE, MessageStoreService.ProcStates.GLB_ProcState_OutboundBeforeTransformation);
            final SoapEnvelope transformedResponse = oasysRiskUpdateTransformer.oasysRiskUpdateResponseOf(response, maybeOasysRiskUpdate);
            final String transformedResponseXmlOf = commonTransformer.transformedResponseXmlOf(transformedResponse);
            messageStoreService.writeMessage(transformedResponseXmlOf, correlationId, offenderId, NDH_WEB_SERVICE_RISK_UPDATE, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation);
            return transformedResponseXmlOf;
        }
    }


    private Optional<String> mappingSoapFault(String correlationId) {
        return Optional.of(faultTransformer.mappingSoapFaultOf(correlationId));
    }

    private Optional<DeliusRiskUpdateResponse> deliusRiskUpdateResponseOf(Optional<String> maybeRawResponse, String correlationId, String offenderId) {
        return maybeRawResponse.flatMap(rawResponse -> {
            messageStoreService.writeMessage(rawResponse, correlationId, offenderId, NDH_WEB_SERVICE_RISK_UPDATE, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation);
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

    private Optional<SoapEnvelope> deliusRiskUpdateRequestOf(String updateXml, Optional<SoapEnvelope> maybeOasysRiskUpdate, String correlationId, String offenderId) {
        return maybeOasysRiskUpdate.map(oasysRiskUpdate -> {
            messageStoreService.writeMessage(updateXml,
                    correlationId,
                    offenderId,
                    NDH_WEB_SERVICE_RISK_UPDATE,
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
