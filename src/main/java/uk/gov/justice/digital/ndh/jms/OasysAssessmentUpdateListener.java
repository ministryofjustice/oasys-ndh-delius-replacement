package uk.gov.justice.digital.ndh.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.IdempotentLogger;
import uk.gov.justice.digital.ndh.service.MessageStoreService;
import uk.gov.justice.digital.ndh.service.OasysAssessmentService;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.transforms.OasysAssessmentUpdateTransformer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.Optional;

import static uk.gov.justice.digital.ndh.ThatsNotMyNDH.ASSESSMENT_PROCESS;
import static uk.gov.justice.digital.ndh.config.JmsConfig.OASYS_MESSAGES;

@Component
@Slf4j
public class OasysAssessmentUpdateListener {

    private final OasysAssessmentUpdateTransformer oasysAssessmentUpdateTransformer;
    private final XmlMapper xmlMapper;
    private final OasysAssessmentService oasysAssessmentService;
    private final MessageStoreService messageStoreService;
    private final ExceptionLogService exceptionLogService;
    private final IdempotentLogger idempotentLogger;

    @Autowired
    public OasysAssessmentUpdateListener(OasysAssessmentUpdateTransformer oasysAssessmentUpdateTransformer, XmlMapper xmlMapper, OasysAssessmentService oasysAssessmentService, MessageStoreService messageStoreService, ExceptionLogService exceptionLogService, IdempotentLogger idempotentLogger) {
        this.oasysAssessmentUpdateTransformer = oasysAssessmentUpdateTransformer;
        this.xmlMapper = xmlMapper;
        this.oasysAssessmentService = oasysAssessmentService;
        this.messageStoreService = messageStoreService;
        this.exceptionLogService = exceptionLogService;
        this.idempotentLogger = idempotentLogger;
    }

    private String readMessage(Message message) throws JMSException {
        return ((TextMessage) message).getText();
    }

    private Optional<SoapEnvelopeSpec1_2> buildDeliusSoapEnvelope(Optional<SoapEnvelopeSpec1_2> maybeNdhSoapMessage) {
        return maybeNdhSoapMessage.map(oasysAssessmentUpdateTransformer::deliusAssessmentUpdateOf);
    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(Message message) throws UnirestException, DocumentException {
        log.info("HANDLING MESSAGE {}", message.toString());

        Optional<String> maybeSoapXmlFromOasys = readFromQueue(message);

        val correlationId = oasysAssessmentUpdateTransformer.correlationIdOf(maybeSoapXmlFromOasys.orElse("?"));
        val offenderId = oasysAssessmentUpdateTransformer.customIdOf(maybeSoapXmlFromOasys.orElse("?"));

        maybeSoapXmlFromOasys.ifPresent(xml -> logIdempotent(message, xml, correlationId, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation));

        Optional<SoapEnvelopeSpec1_2> maybeInputSoapMessage = buildNdhSoapEnvelope(maybeSoapXmlFromOasys, correlationId, message);

        Optional<SoapEnvelopeSpec1_2> maybeDeliusRequest = Optional.empty();

        try {
            maybeDeliusRequest = buildDeliusSoapEnvelope(maybeInputSoapMessage);
        } catch (NDHMappingException ndhme) {
            log.error("Mapping fail: {}", ndhme.toString());
            exceptionLogService.logMappingFail(ndhme.getCode(), ndhme.getValue(), ndhme.getSubject(), correlationId, offenderId);
        }

        Optional<String> maybeRawDeliusRequest = rawDeliusRequestOf(maybeDeliusRequest, message, correlationId);

        maybeRawDeliusRequest.ifPresent(xml -> logIdempotent(message, xml, correlationId, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation));

        Optional<String> maybeRawDeliusResponse = rawDeliusResponseOf(maybeRawDeliusRequest, maybeDeliusRequest, maybeSoapXmlFromOasys, message);

        handleDeliusResponse(maybeRawDeliusResponse, maybeDeliusRequest);
    }

    private void logIdempotent(Message message, String xml, String correlationId, String offenderId, MessageStoreService.ProcStates procState) {
        try {
            if (!message.getJMSRedelivered()) {
                messageStoreService.writeMessage(
                        xml,
                        correlationId,
                        offenderId,
                        ASSESSMENT_PROCESS,
                        procState);
            }
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }

    private void handleDeliusResponse(Optional<String> maybeRawDeliusResponse, Optional<SoapEnvelopeSpec1_2> maybeDeliusRequest) {

        maybeRawDeliusResponse.map(
                rawDeliusResponse -> {

                    SoapEnvelopeSpec1_2 deliusResponse = null;

                    try {
                        deliusResponse = xmlMapper.readValue(rawDeliusResponse, SoapEnvelopeSpec1_2.class);
                        if (deliusResponse.getBody().isSoapFault()) {
                            log.error("Delius SOAP fault: {}", rawDeliusResponse);
                            exceptionLogService.logFault(rawDeliusResponse, maybeDeliusRequest.get().getHeader().getHeader().getMessageId(), "SOAP Fault from Delius");
                        }
                    } catch (IOException e) {
                        log.error("Fail reading delius reponse: {}", e.getMessage());
                        exceptionLogService.logFault(rawDeliusResponse, maybeDeliusRequest.get().getHeader().getHeader().getMessageId(), "Garbage response from Delius");
                    }

                    return deliusResponse;
                }
        );
    }

    private Optional<String> rawDeliusResponseOf(Optional<String> maybeRawDeliusRequest, Optional<SoapEnvelopeSpec1_2> maybeDeliusRequest, Optional<String> maybeSoapXmlFromOasys, Message message) throws UnirestException {

        if (maybeRawDeliusRequest.isPresent()) {
            try {
                return Optional.of(oasysAssessmentService.deliusWebServiceResponseOf(maybeRawDeliusRequest.get()));
            } catch (UnirestException e) {
                log.error("No response from Delius: {} Rejecting message {}", e.getMessage(), message);
                idempotentLogger.errorIdempotent(maybeDeliusRequest.get().getHeader().getHeader().getMessageId(), maybeSoapXmlFromOasys, message, "No response from Delius");
                throw e;
            }
        } else {
            return Optional.empty();
        }

    }



    private Optional<String> rawDeliusRequestOf(Optional<SoapEnvelopeSpec1_2> maybeDeliusRequest, Message message, String correlationId) {

        final Optional<String> maybeRawDeliusRequest = maybeDeliusRequest.map(deliusAssessmentUpdateSoapEnvelope -> {

            String rawDeliusRequest = null;
            try {
                rawDeliusRequest = xmlMapper.writeValueAsString(deliusAssessmentUpdateSoapEnvelope);
            } catch (JsonProcessingException e) {
                log.error("Can't serialize request to Delius. Ignore and continue: {}", e.getMessage());
                idempotentLogger.errorIdempotent(correlationId, Optional.of(deliusAssessmentUpdateSoapEnvelope.toString()), message, "Can't serialize request to Delius.");
            }

            return rawDeliusRequest;
        });

        return maybeRawDeliusRequest;
    }

    private Optional<SoapEnvelopeSpec1_2> buildNdhSoapEnvelope(Optional<String> maybeSoapXmlFromOasys, String correlationId, Message message) {

        return maybeSoapXmlFromOasys.map(soapXmlFromOasys -> {
            try {
                final SoapEnvelopeSpec1_2 soapEnvelope = xmlMapper.readValue(soapXmlFromOasys, SoapEnvelopeSpec1_2.class);

                if (soapEnvelope.getBody() == null) {
                    log.error("Input message has no SOAP body. Ignore and continue: {}", soapXmlFromOasys);
                    idempotentLogger.errorIdempotent(correlationId, Optional.of(soapXmlFromOasys), message, "Input message has no SOAP body");
                    return null;
                }

                return soapEnvelope;
            } catch (IOException e) {
                log.error("Can't parse input message. Ignore and continue: {}", e.getMessage());
                idempotentLogger.errorIdempotent(correlationId, Optional.of(soapXmlFromOasys), message, "Can't parse input message");
                return null;
            }
        });

    }

    private Optional<String> readFromQueue(Message message) {
        String soapXmlFromOasys;
        try {
            soapXmlFromOasys = readMessage(message);
        } catch (JMSException e) {
            log.error("Can't read input message from queue. Ignore and continue. {}", e.getMessage());
            return Optional.empty();
        }

        return Optional.of(soapXmlFromOasys);
    }
}
