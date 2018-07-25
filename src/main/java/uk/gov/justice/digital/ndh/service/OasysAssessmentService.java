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
    private final DeliusClient deliusClient;

    @Autowired
    public OasysAssessmentService(JmsTemplate jmsTemplate,
                                  Queue oasysMessagesQueue,
                                  DeliusClient deliusClient) {
        this.jmsTemplate = jmsTemplate;
        this.oasysMessagesQueue = oasysMessagesQueue;
        this.deliusClient = deliusClient;
    }

    public void publishUpdate(String updateXml) {
        jmsTemplate.convertAndSend(oasysMessagesQueue, updateXml);
    }

    public String deliusWebServiceResponseOf(String deliusSoapXml) throws UnirestException {
        return deliusClient.deliusWebServiceResponseOf(deliusSoapXml);
    }
}
