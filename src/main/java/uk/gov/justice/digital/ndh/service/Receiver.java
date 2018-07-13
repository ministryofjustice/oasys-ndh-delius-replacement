package uk.gov.justice.digital.ndh.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @JmsListener(destination = "oasysMessages", containerFactory = "myFactory")
    public void receiveMessage(String msg) {
        System.out.println("Received <" + msg + ">");
    }

}