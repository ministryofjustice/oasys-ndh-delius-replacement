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
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
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

    @Autowired
    public OasysAssessmentUpdateListener(OasysAssessmentUpdateTransformer oasysAssessmentUpdateTransformer, XmlMapper xmlMapper, OasysAssessmentService oasysAssessmentService, MessageStoreService messageStoreService, ExceptionLogService exceptionLogService) {
        this.oasysAssessmentUpdateTransformer = oasysAssessmentUpdateTransformer;
        this.xmlMapper = xmlMapper;
        this.oasysAssessmentService = oasysAssessmentService;
        this.messageStoreService = messageStoreService;
        this.exceptionLogService = exceptionLogService;
    }

    private String readMessage(Message message) throws JMSException {
        return ((TextMessage) message).getText();
    }

    private Optional<SoapEnvelope> buildDeliusSoapEnvelope(Optional<SoapEnvelope> maybeNdhSoapMessage) {
        return maybeNdhSoapMessage.map(oasysAssessmentUpdateTransformer::deliusAssessmentUpdateOf);
    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1")
    public void onMessage(Message message) throws UnirestException, JMSException, DocumentException {
        log.info("HANDLING MESSAGE {}", message.toString());

        Optional<String> maybeSoapXmlFromOasys = readFromQueue(message);

        val correlationId = oasysAssessmentUpdateTransformer.correlationIdOf(maybeSoapXmlFromOasys.orElse("?"));
        val offenderId = oasysAssessmentUpdateTransformer.customIdOf(maybeSoapXmlFromOasys.orElse("?"));

        maybeSoapXmlFromOasys.ifPresent(xml -> logIdempotent(message, xml, correlationId, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation));

        Optional<SoapEnvelope> maybeInputSoapMessage = buildNdhSoapEnvelope(maybeSoapXmlFromOasys, correlationId, message);

        Optional<SoapEnvelope> maybeDeliusRequest = Optional.empty();

        try {
            maybeDeliusRequest = buildDeliusSoapEnvelope(maybeInputSoapMessage);
        }  catch (NDHMappingException ndhme) {
            exceptionLogService.logMappingFail(ndhme.getCode(), ndhme.getSourceValue(), ndhme.getSubject(), correlationId, offenderId);
        }

        Optional<String> maybeRawDeliusRequest = rawDeliusRequestOf(maybeDeliusRequest, message, correlationId, offenderId);

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

    private Optional<SoapEnvelope> handleDeliusResponse(Optional<String> maybeRawDeliusResponse, Optional<SoapEnvelope> maybeDeliusRequest) {

        return maybeRawDeliusResponse.map(
                rawDeliusResponse -> {

                    SoapEnvelope deliusResponse = null;

                    try {
                        deliusResponse = xmlMapper.readValue(rawDeliusResponse, SoapEnvelope.class);
                        if (deliusResponse.getBody().isSoapFault()) {
                            exceptionLogService.logFault(rawDeliusResponse, maybeDeliusRequest.get().getHeader().getHeader().getMessageId(), "SOAP Fault from Delius");
                        }
                    } catch (IOException e) {
                        exceptionLogService.logFault(rawDeliusResponse, maybeDeliusRequest.get().getHeader().getHeader().getMessageId(), "Garbage response from Delius");
                    }

                    return deliusResponse;
                }
        );
    }

    private Optional<String> rawDeliusResponseOf(Optional<String> maybeRawDeliusRequest, Optional<SoapEnvelope> maybeDeliusRequest, Optional<String> maybeSoapXmlFromOasys, Message message) throws JMSException, UnirestException {

        if (maybeRawDeliusRequest.isPresent()) {
            try {
                return Optional.of(oasysAssessmentService.deliusWebServiceResponseOf(maybeRawDeliusRequest.get()));
            } catch (UnirestException e) {
                log.error("No response from Delius: {} Rejecting message {}", e.getMessage(), message);
                errorIdempotent(maybeDeliusRequest.get().getHeader().getHeader().getMessageId(), maybeSoapXmlFromOasys, message, "No response from Delius");
                throw e;
            }
        } else {
            return Optional.empty();
        }

    }

    private void errorIdempotent(String correlationId, Optional<String> maybeSoapXml, Message message, String description) {
        try {
            if (!message.getJMSRedelivered()) {
                exceptionLogService.logFault(maybeSoapXml.get(), correlationId, description);
            }
        } catch (JMSException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(maybeSoapXml.get(), correlationId, e.getMessage());
        }
    }

    private Optional<String> rawDeliusRequestOf(Optional<SoapEnvelope> maybeDeliusRequest, Message message, String correlationId, String offenderId) throws JMSException {

        final Optional<String> maybeRawDeliusRequest = maybeDeliusRequest.map(deliusAssessmentUpdateSoapEnvelope -> {

            String rawDeliusRequest = null;
            try {
                rawDeliusRequest = xmlMapper.writeValueAsString(deliusAssessmentUpdateSoapEnvelope);
            } catch (JsonProcessingException e) {
                log.error("Can't serialize request to Delius. Ignore and continue: {}", e.getMessage());
                errorIdempotent(correlationId, Optional.of(deliusAssessmentUpdateSoapEnvelope.toString()), message, "Can't serialize request to Delius.");
            }

            return rawDeliusRequest;
        });

        if (maybeRawDeliusRequest == null) {
            return Optional.empty();
        }

        return maybeRawDeliusRequest;
    }

    private Optional<SoapEnvelope> buildNdhSoapEnvelope(Optional<String> maybeSoapXmlFromOasys, String correlationId, Message message) {

        final Optional<SoapEnvelope> maybeNdhAssessmentUpdateSoapEnvelope = maybeSoapXmlFromOasys.map(soapXmlFromOasys -> {
            try {
                final SoapEnvelope soapEnvelope = xmlMapper.readValue(soapXmlFromOasys, SoapEnvelope.class);

                if (soapEnvelope.getBody() == null) {
                    log.error("Input message has no SOAP body. Ignore and continue: {}", soapXmlFromOasys);
                    errorIdempotent(correlationId, Optional.ofNullable(soapXmlFromOasys), message, "Input message has no SOAP body");
                    return null;
                }

                return soapEnvelope;
            } catch (IOException e) {
                log.error("Can't parse input message. Ignore and continue: {}", e.getMessage());
                errorIdempotent(correlationId, Optional.ofNullable(soapXmlFromOasys), message, "Can't parse input message");
                return null;
            }
        });

        if (maybeNdhAssessmentUpdateSoapEnvelope == null) {
            return Optional.empty();
        }

        return maybeNdhAssessmentUpdateSoapEnvelope;

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
