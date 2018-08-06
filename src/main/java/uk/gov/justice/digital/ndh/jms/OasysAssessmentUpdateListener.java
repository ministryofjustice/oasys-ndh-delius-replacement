package uk.gov.justice.digital.ndh.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusAssessmentSummaryResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;
import uk.gov.justice.digital.ndh.service.OasysAssessmentService;
import uk.gov.justice.digital.ndh.service.transforms.OasysAssessmentUpdateTransformer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.Optional;

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

    private Optional<DeliusAssessmentUpdateSoapEnvelope> buildOasysSoapEnvelope(Optional<SoapEnvelope> maybeNdhSoapMessage) {
        return maybeNdhSoapMessage.map(oasysAssessmentUpdateTransformer::deliusAssessmentUpdateOf);
    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1")
    public void onMessage(Message message) throws UnirestException, JMSException {
        log.info("HANDLING MESSAGE {}", message.toString());

        Optional<String> maybeSoapXmlFromOasys = readFromQueue(message);

        Optional<SoapEnvelope> maybeInputSoapMessage = buildNdhSoapEnvelope(maybeSoapXmlFromOasys, message);

        Optional<DeliusAssessmentUpdateSoapEnvelope> maybeDeliusRequest = buildOasysSoapEnvelope(maybeInputSoapMessage);

        Optional<String> maybeRawDeliusRequest = rawDeliusRequestOf(maybeDeliusRequest, message);

        Optional<String> maybeRawDeliusResponse = rawDeliusResponseOf(maybeRawDeliusRequest, maybeDeliusRequest, maybeSoapXmlFromOasys, message);

        handleDeliusResponse(maybeRawDeliusResponse, maybeDeliusRequest);
    }

    private Optional<DeliusAssessmentSummaryResponse> handleDeliusResponse(Optional<String> maybeRawDeliusResponse, Optional<DeliusAssessmentUpdateSoapEnvelope> maybeDeliusRequest) {

        return maybeRawDeliusResponse.map(
                rawDeliusResponse -> {

                    DeliusAssessmentSummaryResponse deliusResponse = null;

                    try {
                        deliusResponse = xmlMapper.readValue(rawDeliusResponse, DeliusAssessmentSummaryResponse.class);
                        if (deliusResponse.isBadResponse()) {
                            log.error("Bad response from Delius: {}", rawDeliusResponse);
                            exceptionLogService.logFault(rawDeliusResponse, maybeDeliusRequest.get().getHeader().getCommonHeader().getMessageId(), "Bad response from Delius");
                        }
                    } catch (IOException e) {
                        log.error("Garbage response from Delius: {}", e.getMessage());
                        exceptionLogService.logFault(rawDeliusResponse, maybeDeliusRequest.get().getHeader().getCommonHeader().getMessageId(), "Garbage response from Delius");
                    }

                    return deliusResponse;
                }
        );
    }

    private Optional<String> rawDeliusResponseOf(Optional<String> maybeRawDeliusRequest, Optional<DeliusAssessmentUpdateSoapEnvelope> maybeDeliusRequest, Optional<String> maybeSoapXmlFromOasys, Message message) throws JMSException, UnirestException {

        boolean redelivered = message.getJMSRedelivered();

        if (maybeRawDeliusRequest.isPresent()) {
            try {
                return Optional.of(oasysAssessmentService.deliusWebServiceResponseOf(maybeRawDeliusRequest.get()));
            } catch (UnirestException e) {
                log.error("No response from Delius: {} Rejecting message {}", e.getMessage(), message);
                if (!redelivered) {
                    exceptionLogService.logFault(maybeSoapXmlFromOasys.get(), maybeDeliusRequest.get().getHeader().getCommonHeader().getMessageId(), "No response from Delius");
                }
                throw e;
            }
        } else {
            return Optional.empty();
        }

    }

    private Optional<String> rawDeliusRequestOf(Optional<DeliusAssessmentUpdateSoapEnvelope> maybeDeliusRequest, Message message) throws JMSException {

        final boolean redelivered = message.getJMSRedelivered();

        final Optional<String> maybeRawDeliusRequest = maybeDeliusRequest.map(deliusAssessmentUpdateSoapEnvelope -> {

            String rawDeliusRequest = null;
            try {
                rawDeliusRequest = xmlMapper.writeValueAsString(deliusAssessmentUpdateSoapEnvelope);
                if (!redelivered) {
                    messageStoreService.writeMessage(rawDeliusRequest, deliusAssessmentUpdateSoapEnvelope.getHeader().getCommonHeader().getMessageId(), MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation);
                }
            } catch (JsonProcessingException e) {
                log.error("Can't serialize request to Delius. Ignore and continue: {}", e.getMessage());
                exceptionLogService.logFault(deliusAssessmentUpdateSoapEnvelope.toString(), deliusAssessmentUpdateSoapEnvelope.getHeader().getCommonHeader().getMessageId(), "Can't serialize request to Delius");
            }

            return rawDeliusRequest;
        });

        if (maybeDeliusRequest == null) {
            return Optional.empty();
        }

        return maybeRawDeliusRequest;
    }

    private Optional<SoapEnvelope> buildNdhSoapEnvelope(Optional<String> maybeSoapXmlFromOasys, Message message) {

        final Optional<SoapEnvelope> maybeNdhAssessmentUpdateSoapEnvelope = maybeSoapXmlFromOasys.map(soapXmlFromOasys -> {
            try {
                return xmlMapper.readValue(soapXmlFromOasys, SoapEnvelope.class);
            } catch (IOException e) {
                log.error("Can't parse input message. Ignore and continue: {}", e.getMessage());
                exceptionLogService.logFault(soapXmlFromOasys, null, "Can't parse input message");
                return null;
            }
        });

        if (maybeNdhAssessmentUpdateSoapEnvelope == null) {
            return Optional.empty();
        }

        return maybeNdhAssessmentUpdateSoapEnvelope;

    }

    private Optional<String> readFromQueue(Message message) throws JMSException {
        String soapXmlFromOasys;
        try {
            soapXmlFromOasys = readMessage(message);
        } catch (JMSException e) {
            log.error("Can't read input message from queue. Ignore and continue. {}", e.getMessage());
            return Optional.empty();
        }

        if (!message.getJMSRedelivered()) {
            messageStoreService.writeMessage(soapXmlFromOasys, null, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);
        }

        return Optional.of(soapXmlFromOasys);
    }
}
