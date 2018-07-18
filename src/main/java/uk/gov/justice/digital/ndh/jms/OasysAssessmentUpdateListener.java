package uk.gov.justice.digital.ndh.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.NdhAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.service.OasysAssessmentService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.justice.digital.ndh.config.JmsConfig.OASYS_MESSAGES;

@Component
@Slf4j
public class OasysAssessmentUpdateListener {

    private final XmlMapper xmlMapper;
    private final Function<Optional<DeliusResponse>, Boolean> shouldAcknowledge = Optional::isPresent;
    private final OasysAssessmentService oasysAssessmentService;
    private final Function<Optional<DeliusResponse>, Optional<DeliusResponse>> faultHandler;

    @Autowired
    public OasysAssessmentUpdateListener(XmlMapper xmlMapper, OasysAssessmentService oasysAssessmentService) {
        this.xmlMapper = xmlMapper;
        this.oasysAssessmentService = oasysAssessmentService;

        faultHandler = maybeDeliusResponse -> {
            maybeDeliusResponse.ifPresent(deliusResponse -> {
                if (deliusResponse.isFault()) {
                    try {
                        oasysAssessmentService.publishFault(deliusResponse);
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                    }
                }
            });

            return maybeDeliusResponse;
        };
    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1")
    public void onMessage(Message message) {

        final val maybeResult = readMessage(message)
                //TODO: Call the transformer, but in the meantime just hard wire
                .map(update -> DeliusAssessmentUpdateSoapEnvelope.builder().build())
                .flatMap(oasysAssessmentService::deliusWebServiceResponseOf);

        if (faultHandler.andThen(shouldAcknowledge).apply(maybeResult)) {
            log.info("Acknowledging message {} : {}", message);

            try {
                message.acknowledge();
            } catch (JMSException e) {
                log.error(e.getMessage());
            }
        }
        else {
            log.info("Rejecting message {} : {}", message);
        }
    }

    private Optional<NdhAssessmentUpdateSoapEnvelope> readMessage(Message message) {
        try {
            return Optional.ofNullable(xmlMapper.readValue(((TextMessage) message).getText(), NdhAssessmentUpdateSoapEnvelope.class));
        } catch (Exception e) {
            log.error("Can't turn message {0} into NdhAssessmentUpdateSoapEnvelope.", message);
        }
        return Optional.empty();
    }
}
