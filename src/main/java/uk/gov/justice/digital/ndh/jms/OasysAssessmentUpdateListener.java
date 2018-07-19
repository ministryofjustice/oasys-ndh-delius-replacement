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
import uk.gov.justice.digital.ndh.service.OasysAssessmentService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;

import static uk.gov.justice.digital.ndh.config.JmsConfig.OASYS_MESSAGES;

@Component
@Slf4j
public class OasysAssessmentUpdateListener {

    private final XmlMapper xmlMapper;
    private final OasysAssessmentService oasysAssessmentService;

    @Autowired
    public OasysAssessmentUpdateListener(XmlMapper xmlMapper, OasysAssessmentService oasysAssessmentService) {
        this.xmlMapper = xmlMapper;
        this.oasysAssessmentService = oasysAssessmentService;
    }

    private String readMessage(Message message) throws JMSException {
        return ((TextMessage) message).getText();
    }

    private DeliusRequest deliusSoapMessageOf(NdhAssessmentUpdateSoapEnvelope ndhSoapMessage) {
        //TODO: Insert proper transform here
        return DeliusRequest.builder().build();

    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1")
    public void onMessage(Message message) throws UnirestException {

        String soapXmlFromOasys;
        try {
            soapXmlFromOasys = readMessage(message);
        } catch (JMSException e) {
            log.error("Can't read input message from queue. Ignore and continue. {}", e.getMessage());
            return;
        }

        NdhAssessmentUpdateSoapEnvelope inputSoapMessage;
        try {
            inputSoapMessage = xmlMapper.readValue(soapXmlFromOasys, NdhAssessmentUpdateSoapEnvelope.class);
        } catch (IOException e) {
            log.error("Can't parse input message. Ignore and continue: {}", e.getMessage());
            return;
        }

        DeliusRequest deliusRequest = deliusSoapMessageOf(inputSoapMessage);

        String rawDeliusResponse;
        try {
            rawDeliusResponse = oasysAssessmentService.deliusWebServiceResponseOf(deliusRequest);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize request to Delius. Ignore and continue: {}", e.getMessage());
            return;
        } catch (UnirestException e) {
            log.error("No response from Delius: {} Rejecting message {}", e.getMessage(), message);
            throw e;
        }

        DeliusResponse deliusResponse;
        try {
            deliusResponse = xmlMapper.readValue(rawDeliusResponse, DeliusResponse.class);
            if (deliusResponse.isBadResponse()) {
                oasysAssessmentService.publishFault(soapXmlFromOasys, rawDeliusResponse);
            }
        } catch (IOException e) {
            log.error("Garbage response from Delius: {}", e.getMessage());
            oasysAssessmentService.publishFault(soapXmlFromOasys, rawDeliusResponse);
        }
    }
}
