package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.exception.OasysAPIServiceError;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class EventsPullerService {

    private final NomisClient custodyApiClient;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    private final XtagTransformer xtagTransformer;
    private final OasysSOAPClient oasysSOAPClient;
    private final ExceptionLogService exceptionLogService;

    public EventsPullerService(NomisClient custodyApiClient, JmsTemplate jmsTemplate, @Qualifier("globalObjectMapper") ObjectMapper objectMapper, OffenderTransformer offenderTransformer, MappingService mappingService, XmlMapper xmlMapper, XtagTransformer xtagTransformer, OasysSOAPClient oasysSOAPClient, ExceptionLogService exceptionLogService) {
        this.custodyApiClient = custodyApiClient;
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
        this.xtagTransformer = xtagTransformer;
        this.oasysSOAPClient = oasysSOAPClient;
        this.exceptionLogService = exceptionLogService;
    }

    @Scheduled(fixedDelayString = "${xtag.poll.period:10000}")
    public void pullEvents() throws ExecutionException, UnirestException {
        final Optional<String> maybePullFrom = getPullFromDateTime();

        final String pullFrom = maybePullFrom.orElse(ZonedDateTime.now().toString());

        final LocalDateTime to = LocalDateTime.now();

        Optional<List<OffenderEvent>> maybeOffenderEvents = custodyApiClient
                .doGetWithRetry("events", ImmutableMap.of("from", pullFrom,
                        "to", to.toString(),
                        "type", "OFFENDER_BOOKING_UPDATE,BASIC_OFFENDER_DETAILS_UPDATE,ESTABLISHMENT_RECEPTION,ESTABLISHMENT_DISCHARGE,SENTENCE_UPDATE,IMPRISONMENT_STATUS_UPDATE"))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asEvents);

        try {
            if (maybeOffenderEvents.isPresent()) {
                handleEvents(maybeOffenderEvents.get());
            }
            setPullFromDateTime(ZonedDateTime.now().toString());
        } catch (Exception e) {
            log.error(e.getMessage());
            setPullFromDateTime(pullFrom);
        }


    }

    private void handleEvents(List<OffenderEvent> events) throws ExecutionException, UnirestException, NomisAPIServiceError, JsonProcessingException, OasysAPIServiceError {
        for (OffenderEvent offenderEvent : events) {
            sendToOAsys(xtagEventMessageOf(offenderEvent));
        }
    }

    private void sendToOAsys(Optional<EventMessage> eventMessage) throws JsonProcessingException, UnirestException, OasysAPIServiceError {
        if (eventMessage.isPresent()) {
            final String oasysSoapXml = xmlMapper.writeValueAsString(eventMessage);
            final HttpResponse<String> response = oasysSOAPClient.oasysWebServiceResponseOf(oasysSoapXml);
            if (response.getStatus() != HttpStatus.OK.value()) {
                exceptionLogService.logFault(oasysSoapXml, eventMessage.get().getCorrelationId(), "Bad response from oasys.");
            }
            if (response.getStatus() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                throw new OasysAPIServiceError("Can't send event message to oasys. Response " + response.getStatus());
            }
        }
    }

    private Optional<EventMessage> xtagEventMessageOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        switch (event.getNomisEventType()) {
            case "BOOK_UPD_OASYS":
                return xtagTransformer.bookingUpdatedXtagOf(event);
            case "OFF_RECEP_OASYS":
                return xtagTransformer.offenderReceptionXtagOf(event);
            case "OFF_DISCH_OASYS":
                return xtagTransformer.offenderDischargeXtagOf(event);
            case "OFF_UPD_OASYS":
                return xtagTransformer.offenderUpdatedXtagOf(event);
            case "OFF_SENT_OASYS":
                return xtagTransformer.offenderSentenceUpdatedXtagOf(event);
            case "OFF_IMP_STAT_OASYS":
                return xtagTransformer.offenderImprisonmentStatusUpdatedXtagOf(event);
        }
        return Optional.empty();
    }


    private List<OffenderEvent> asEvents(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, OffenderEvent[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private Optional<String> getPullFromDateTime() {

        Optional<String> maybeLastPolled;

        try {
            maybeLastPolled = Optional.ofNullable(jmsTemplate.receive("LAST_POLLED")).flatMap(
                    m -> {
                        try {
                            return Optional.ofNullable(((TextMessage) m).getText());
                        } catch (JMSException e) {
                            log.error(e.getMessage());
                            return Optional.empty();
                        }
                    }
            );
        } catch (JmsException jmse) {
            maybeLastPolled = Optional.empty();
        }

        log.info("Last polled date retrieved : {}", maybeLastPolled.orElse("empty"));

        return maybeLastPolled;
    }

    private void setPullFromDateTime(String time) {
        jmsTemplate.convertAndSend("LAST_POLLED", time);
    }
}
