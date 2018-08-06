package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
@Slf4j
public class OasysAssessmentService {

    private final JmsTemplate jmsTemplate;
    private final Queue oasysMessagesQueue;
    private final DeliusAssessmentUpdateClient deliusAssessmentUpdateClient;

    @Autowired
    public OasysAssessmentService(JmsTemplate jmsTemplate,
                                  Queue oasysMessagesQueue,
                                  DeliusAssessmentUpdateClient deliusAssessmentUpdateClient) {
        this.jmsTemplate = jmsTemplate;
        this.oasysMessagesQueue = oasysMessagesQueue;
        this.deliusAssessmentUpdateClient = deliusAssessmentUpdateClient;
    }

    public void publishAssessmentUpdate(String updateXml) {
        jmsTemplate.convertAndSend(oasysMessagesQueue, updateXml);
    }

    public String deliusWebServiceResponseOf(String deliusSoapXml) throws UnirestException {
        return deliusAssessmentUpdateClient.deliusWebServiceResponseOf(deliusSoapXml);
    }
}
