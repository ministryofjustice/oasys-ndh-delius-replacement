package uk.gov.justice.digital.ndh.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRequest;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.NdhAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;
import uk.gov.justice.digital.ndh.service.OasysAssessmentService;
import uk.gov.justice.digital.ndh.service.transtorms.OasysAssessmentUpdateTransformer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;

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

    private DeliusRequest deliusSoapMessageOf(NdhAssessmentUpdateSoapEnvelope ndhSoapMessage) {
        return oasysAssessmentUpdateTransformer.deliusAssessmentUpdateOf(ndhSoapMessage);

    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1")
    public void onMessage(Message message) throws UnirestException, JMSException {
        log.info("HANDLING MESSAGE {}", message.toString());

        boolean redelivered =  message.getJMSRedelivered();

        String soapXmlFromOasys;
        try {
            soapXmlFromOasys = readMessage(message);
        } catch (JMSException e) {
            log.error("Can't read input message from queue. Ignore and continue. {}", e.getMessage());
            return;
        }

        if (!redelivered) {
            messageStoreService.writeMessage(soapXmlFromOasys, null, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);
        }

        NdhAssessmentUpdateSoapEnvelope inputSoapMessage;
        try {
            inputSoapMessage = xmlMapper.readValue(soapXmlFromOasys, NdhAssessmentUpdateSoapEnvelope.class);
        } catch (IOException e) {
            log.error("Can't parse input message. Ignore and continue: {}", e.getMessage());
            exceptionLogService.logFault(soapXmlFromOasys, null, "Can't parse input message");
            return;
        }

        DeliusRequest deliusRequest = deliusSoapMessageOf(inputSoapMessage);

        String rawDeliusRequest;
        try {
            rawDeliusRequest = xmlMapper.writeValueAsString(deliusRequest);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize request to Delius. Ignore and continue: {}", e.getMessage());
            exceptionLogService.logFault(deliusRequest.toString(), deliusRequest.getHeader().getCommonHeader().getMessageId(), "Can't serialize request to Delius");
            return;
        }

        if (!redelivered) {
            messageStoreService.writeMessage(rawDeliusRequest, deliusRequest.getHeader().getCommonHeader().getMessageId(), MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation);
        }

        String rawDeliusResponse;
        try {
            rawDeliusResponse = oasysAssessmentService.deliusWebServiceResponseOf(rawDeliusRequest);
        } catch (UnirestException e) {
            log.error("No response from Delius: {} Rejecting message {}", e.getMessage(), message);
            if (!redelivered) {
                exceptionLogService.logFault(soapXmlFromOasys, deliusRequest.getHeader().getCommonHeader().getMessageId(), "No response from Delius");
            }
            throw e;
        }

        DeliusResponse deliusResponse;
        try {
            deliusResponse = xmlMapper.readValue(rawDeliusResponse, DeliusResponse.class);
            if (deliusResponse.isBadResponse()) {
                log.error("Bad response from Delius: {}", rawDeliusResponse);
                exceptionLogService.logFault(rawDeliusResponse, deliusRequest.getHeader().getCommonHeader().getMessageId(), "Bad response from Delius");
            }
        } catch (IOException e) {
            log.error("Garbage response from Delius: {}", e.getMessage());
            exceptionLogService.logFault(rawDeliusResponse, deliusRequest.getHeader().getCommonHeader().getMessageId(), "Garbage response from Delius");
        }
    }
}
