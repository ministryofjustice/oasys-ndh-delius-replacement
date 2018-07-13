package uk.gov.justice.digital.ndh.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.ndh.api.oasys.CmsUpdate;

import javax.jms.Queue;

@RestController
@Slf4j
public class OasysAssessmentController {

    private final JmsTemplate jmsTemplate;
    private final Queue oasysMessagesQueue;


    @Autowired
    public OasysAssessmentController(JmsTemplate jmsTemplate, Queue oasysMessagesQueue) {
        this.jmsTemplate = jmsTemplate;
        this.oasysMessagesQueue = oasysMessagesQueue;
    }

    @RequestMapping(path = "/oasysAssessments", method = RequestMethod.POST, consumes = {"application/xml", "text/xml", "text/plain"})
    public ResponseEntity<Void> handleOasysAssessment(@RequestBody CmsUpdate oasysAssessment) {


        jmsTemplate.convertAndSend(oasysMessagesQueue, oasysAssessment);

        return new ResponseEntity<>(HttpStatus.OK);

    }

}
