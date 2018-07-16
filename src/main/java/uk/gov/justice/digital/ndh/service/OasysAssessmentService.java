package uk.gov.justice.digital.ndh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
public class OasysAssessmentService {

    private final JmsTemplate jmsTemplate;
    private final Queue oasysMessagesQueue;

    @Autowired
    public OasysAssessmentService(JmsTemplate jmsTemplate, Queue oasysMessagesQueue) {
        this.jmsTemplate = jmsTemplate;
        this.oasysMessagesQueue = oasysMessagesQueue;
    }

    public void publishUpdate(String updateXml) {
        jmsTemplate.convertAndSend(oasysMessagesQueue, updateXml);
    }
}
