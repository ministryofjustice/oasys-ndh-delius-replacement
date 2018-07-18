package uk.gov.justice.digital.ndh.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.gov.justice.digital.ndh.config.JmsConfig.OASYS_MESSAGES;

@Component
@Slf4j
public class OasysAssessmentUpdateListener {

    private static final BiFunction<Optional<DeliusResponse>, Message, Optional<Message>> MESSAGE_ACKNOWLEDGER = (maybeDeliusResponse, msg) -> maybeDeliusResponse.map(deliusResponse -> msg);
    private final Function<Optional<DeliusResponse>, Optional<DeliusResponse>> faultHandler;
    private final Function<Message, Optional<NdhAssessmentUpdateSoapEnvelope>> readMessage;
    private final Function<Optional<NdhAssessmentUpdateSoapEnvelope>, Optional<DeliusAssessmentUpdateSoapEnvelope>> transformMessage = maybeNdhSoapEnvelope ->
            //TODO: Call the transformer, but in the meantime just hard wire
            maybeNdhSoapEnvelope.map(update -> DeliusAssessmentUpdateSoapEnvelope.builder().build());
    private final Function<Optional<DeliusAssessmentUpdateSoapEnvelope>, Optional<DeliusResponse>> sendMessage;

    @Autowired
    public OasysAssessmentUpdateListener(XmlMapper xmlMapper, OasysAssessmentService oasysAssessmentService) {

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

        readMessage = message -> {
            try {
                return Optional.ofNullable(xmlMapper.readValue(((TextMessage) message).getText(), NdhAssessmentUpdateSoapEnvelope.class));
            } catch (Exception e) {
                log.error("Can't turn message {0} into NdhAssessmentUpdateSoapEnvelope.", message);
            }
            return Optional.empty();
        };

        sendMessage = maybeDeliusSoapEnvelope -> maybeDeliusSoapEnvelope.flatMap(oasysAssessmentService::deliusWebServiceResponseOf);
    }

    @JmsListener(destination = OASYS_MESSAGES, concurrency = "1")
    public void onMessage(Message message) {

        final Optional<Message> maybeMessage = MESSAGE_ACKNOWLEDGER.apply(readMessage
                .andThen(transformMessage)
                .andThen(sendMessage)
                .andThen(faultHandler)
                .apply(message), message);

        final Supplier<Message> reject = () -> {
            log.error("Rejecting message {} : {}", message);
            return null;
        };

        maybeMessage.map(accept(message)).orElseGet(reject);

    }

    private Function<Message, Object> accept(Message message) {
        return message1 -> {
            log.info("Acknowledging message {} : {}", message1);
            try {
                message.acknowledge();
            } catch (JMSException e) {
                log.error(e.getMessage());
            }
            return null;
        };
    }
}
