package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRequest;

import javax.jms.Queue;

@Service
@Slf4j
public class OasysAssessmentService {

    private final JmsTemplate jmsTemplate;
    private final Queue oasysMessagesQueue;
    private final String ndeliusUrl;
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public OasysAssessmentService(JmsTemplate jmsTemplate,
                                  Queue oasysMessagesQueue,
                                  @Value("${ndelius.assessment.update.url}") String ndeliusUrl,
                                  XmlMapper xmlMapper, @Qualifier("globalObjectMapper") ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.oasysMessagesQueue = oasysMessagesQueue;
        this.ndeliusUrl = ndeliusUrl;
        this.xmlMapper = xmlMapper;
        this.objectMapper = objectMapper;
    }

    public void publishFault(String request, String response) {
        //TODO: Something more useful.
        log.error("Got a bad thing back from Delius. \nRequest: {} \nResponse: {}", request, response);
    }

    public void publishUpdate(String updateXml) {
        jmsTemplate.convertAndSend(oasysMessagesQueue, updateXml);
    }

    public String deliusWebServiceResponseOf(DeliusRequest transformed) throws JsonProcessingException, UnirestException {
        return Unirest.post(ndeliusUrl)
                .body(xmlMapper.writeValueAsString(transformed))
                .asString().getBody();
    }
}
