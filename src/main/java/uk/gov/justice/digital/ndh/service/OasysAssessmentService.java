package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusResponse;

import javax.jms.Queue;
import java.io.IOException;
import java.util.Optional;

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

    public void publishFault(DeliusResponse deliusResponse) throws JsonProcessingException {
        //TODO: Something more useful.
        log.error("Got SOAP Fault back from Delius: {}", objectMapper.writeValueAsString(deliusResponse));
    }

    public void publishUpdate(String updateXml) {
        jmsTemplate.convertAndSend(oasysMessagesQueue, updateXml);
    }

    public Optional<DeliusResponse> deliusWebServiceResponseOf(DeliusAssessmentUpdateSoapEnvelope transformed) {
        try {
            final HttpResponse<String> stringHttpResponse = Unirest.post(ndeliusUrl)
                    .body(xmlMapper.writeValueAsString(transformed))
                    .asString();

            return Optional.of(xmlMapper.readValue(stringHttpResponse.getBody(), DeliusResponse.class));
        } catch (UnirestException | IOException e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

}
