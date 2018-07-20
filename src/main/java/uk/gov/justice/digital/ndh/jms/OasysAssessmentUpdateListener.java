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

    @Autowired
    public OasysAssessmentUpdateListener(OasysAssessmentUpdateTransformer oasysAssessmentUpdateTransformer, XmlMapper xmlMapper, OasysAssessmentService oasysAssessmentService) {
        this.oasysAssessmentUpdateTransformer = oasysAssessmentUpdateTransformer;
        this.xmlMapper = xmlMapper;
        this.oasysAssessmentService = oasysAssessmentService;
    }

    private String readMessage(Message message) throws JMSException {
        return ((TextMessage) message).getText();
    }

    private DeliusRequest deliusSoapMessageOf(NdhAssessmentUpdateSoapEnvelope ndhSoapMessage) {
        return oasysAssessmentUpdateTransformer.deliusAssessmentUpdateOf(ndhSoapMessage);

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
            oasysAssessmentService.publishFault(soapXmlFromOasys, null, null);
            return;
        }

        DeliusRequest deliusRequest = deliusSoapMessageOf(inputSoapMessage);

        String rawDeliusRequest;
        try {
            rawDeliusRequest = xmlMapper.writeValueAsString(deliusRequest);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize request to Delius. Ignore and continue: {}", e.getMessage());
            oasysAssessmentService.publishFault(soapXmlFromOasys, deliusRequest.toString(), null);
            return;
        }

        String rawDeliusResponse;
        try {
            rawDeliusResponse = oasysAssessmentService.deliusWebServiceResponseOf(rawDeliusRequest);
        } catch (UnirestException e) {
            log.error("No response from Delius: {} Rejecting message {}", e.getMessage(), message);
            throw e;
        }

        DeliusResponse deliusResponse;
        try {
            deliusResponse = xmlMapper.readValue(rawDeliusResponse, DeliusResponse.class);
            if (deliusResponse.isBadResponse()) {
                oasysAssessmentService.publishFault(soapXmlFromOasys, rawDeliusRequest, rawDeliusResponse);
            }
        } catch (IOException e) {
            log.error("Garbage response from Delius: {}", e.getMessage());
            oasysAssessmentService.publishFault(soapXmlFromOasys, rawDeliusRequest, rawDeliusResponse);
        }
    }
}
