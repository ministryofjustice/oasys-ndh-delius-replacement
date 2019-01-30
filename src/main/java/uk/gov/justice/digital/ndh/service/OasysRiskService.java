package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;
import uk.gov.justice.digital.ndh.service.transforms.OasysRiskUpdateTransformer;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class OasysRiskService extends RequestResponseService {

    private static final String NDH_WEB_SERVICE_RISK_UPDATE = "NDH_Web_Service_Risk_Update";
    private final OasysRiskUpdateTransformer oasysRiskUpdateTransformer;
    private final DeliusSOAPClient deliusRiskUpdateClient;


    @Autowired
    public OasysRiskService(OasysRiskUpdateTransformer oasysRiskUpdateTransformer,
                            CommonTransformer commonTransformer,
                            XmlMapper xmlMapper,
                            MessageStoreService messageStoreService,
                            ExceptionLogService exceptionLogService,
                            @Qualifier("riskUpdateClient") DeliusSOAPClient deliusRiskUpdateClient,
                            FaultTransformer faultTransformer) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper, faultTransformer);
        this.oasysRiskUpdateTransformer = oasysRiskUpdateTransformer;
        this.deliusRiskUpdateClient = deliusRiskUpdateClient;
    }

    public Optional<String> processRiskUpdate(String updateXml) {

        val maybeOasysRiskUpdate = commonTransformer.asSoapEnvelope(updateXml);

        val cmsProbNumber = maybeOasysRiskUpdate.map(oru -> oru.getBody().getRiskUpdateRequest().getCmsProbNumber()).orElse(null);

        val correlationId = maybeOasysRiskUpdate.map(riskUpdate -> riskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID()).orElse(null);

        logMessage(cmsProbNumber, correlationId, updateXml, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);

        val maybeTransformed = deliusRiskUpdateRequestOf(maybeOasysRiskUpdate);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId);

        maybeTransformedXml.ifPresent(transformedXml -> logMessage(cmsProbNumber, correlationId, transformedXml, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation));

        val maybeRawResponse = rawDeliusUpdateRiskResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusRiskUpdateResponseOf(maybeRawResponse, correlationId);

        if (maybeResponse.isPresent()) {
            if (maybeResponse.get().isSoapFault()) {
                return handleSoapFault(correlationId, maybeRawResponse, maybeResponse.get().toString());
            }
        }

        val maybeOasysSOAPResponse = maybeResponse.flatMap(response -> oasysRiskUpdateTransformer.oasysRiskUpdateResponseOf(Optional.of(response), maybeOasysRiskUpdate));

        maybeRawResponse.ifPresent(xml -> logMessage(cmsProbNumber, correlationId, xml, MessageStoreService.ProcStates.GLB_ProcState_OutboundBeforeTransformation));

        Optional<String> maybeXmlResponse = maybeOasysSOAPResponse.map(oasysResponse -> handleOkResponse(correlationId, oasysResponse));

        maybeXmlResponse.ifPresent(xml -> logMessage(cmsProbNumber, correlationId, xml, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation));

        return maybeXmlResponse;
    }

    private Optional<DeliusRiskUpdateResponse> deliusRiskUpdateResponseOf(Optional<String> maybeRawResponse, String correlationId) {
        return maybeRawResponse.flatMap(rawResponse -> {
            try {
                return Optional.of(xmlMapper.readValue(rawResponse, DeliusRiskUpdateResponse.class));
            } catch (IOException e) {
                log.error("Delius fail: {} {}", e.getMessage(), rawResponse);
                exceptionLogService.logFault(rawResponse, correlationId, "Can't deserialize delius risk update response: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<String> rawDeliusUpdateRiskResponseOf(Optional<String> maybeTransformedXml, String correlationId) {
        return maybeTransformedXml.flatMap((String transformedXml) -> callDeliusRiskUpdate(transformedXml, correlationId));
    }

    private Optional<SoapEnvelopeSpec1_2> deliusRiskUpdateRequestOf(Optional<SoapEnvelopeSpec1_2> maybeOasysRiskUpdate) {
        return maybeOasysRiskUpdate.map(oasysRiskUpdateTransformer::deliusRiskUpdateRequestOf);
    }

    private Optional<String> callDeliusRiskUpdate(String transformedXml, String correlationId) {
        try {
            return Optional.of(deliusRiskUpdateClient.deliusWebServiceResponseOf(transformedXml));
        } catch (UnirestException e) {
            log.error("Delius fail: {} {}", e.getMessage(), transformedXml);
            exceptionLogService.logFault(transformedXml, correlationId, "Can't talk to Delius risk update endpoint: " + e.getMessage());
            return Optional.empty();
        }
    }

    private void logMessage(String cmsProbNUmber, String correlationId, String xml, MessageStoreService.ProcStates procState) {
        messageStoreService.writeMessage(xml, correlationId, cmsProbNUmber, NDH_WEB_SERVICE_RISK_UPDATE, procState);
    }
}