package uk.gov.justice.digital.ndh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/mq")
public class MqController {

    private static final String QUEUE = "oasysMessages";
    @Autowired
    private JmsTemplate jmsTemplate;

    @RequestMapping(path = "/{msg}", method = RequestMethod.GET)
    public String put(@PathVariable String msg) throws JMSException, InterruptedException, ExecutionException {
        jmsTemplate.convertAndSend(QUEUE, msg);
        return "Did " + msg;
    }


}