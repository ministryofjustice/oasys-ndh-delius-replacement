package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.rholder.retry.RetryException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStore;
import uk.gov.justice.digital.ndh.jpa.repository.MessageStoreRepository;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.exception.OasysAPIServiceError;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventsPullerService {

    public static final String OASYS = "OASYS";
    public static final ZoneId LONDON = ZoneId.of("Europe/London");
    private final NomisClient custodyApiClient;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    private final XtagTransformer xtagTransformer;
    private final OasysSOAPClient oasysSOAPClient;
    private final ExceptionLogService exceptionLogService;
    private final MessageStoreService messageStoreService;
    private final MessageStoreRepository messageStoreRepository;
    private final ZonedDateTime startupPullFromDateTime;
    private ZonedDateTime lastPulled;

    public EventsPullerService(NomisClient custodyApiClient,
                               @Qualifier("globalObjectMapper") ObjectMapper objectMapper,
                               XmlMapper xmlMapper,
                               XtagTransformer xtagTransformer,
                               OasysSOAPClient oasysSOAPClient,
                               ExceptionLogService exceptionLogService,
                               MessageStoreService messageStoreService,
                               MessageStoreRepository messageStoreRepository) {
        this.custodyApiClient = custodyApiClient;
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
        this.xtagTransformer = xtagTransformer;
        this.oasysSOAPClient = oasysSOAPClient;
        this.exceptionLogService = exceptionLogService;
        this.messageStoreService = messageStoreService;
        this.messageStoreRepository = messageStoreRepository;
        startupPullFromDateTime = getInitialPullFromDateTime();
        lastPulled = startupPullFromDateTime;
    }

    private static LocalDateTime xtagFudgedTimestampOf(LocalDateTime xtagEnqueueTime) {

        if (LONDON.getRules().isDaylightSavings(xtagEnqueueTime.atZone(LONDON).toInstant())) {
            return xtagEnqueueTime;
        }
        return xtagEnqueueTime.minusHours(1L);
    }

    @Scheduled(fixedDelayString = "${xtag.poll.period:10000}")
    public void pullEvents() {
        final ZonedDateTime now = ZonedDateTime.now();

        log.info("Pulling events from {} to {}", lastPulled, now);

        try {
            Optional<List<OffenderEvent>> maybeOffenderEvents = custodyApiClient
                    .doGetWithRetry("events", ImmutableMap.of("from", lastPulled.toString(),
                            "to", now.toString(),
                            "type", "BOOKING_NUMBER-CHANGED,OFFENDER_MOVEMENT-RECEPTION,OFFENDER_MOVEMENT-DISCHARGE,OFFENDER_BOOKING-CHANGED,OFFENDER_DETAILS-CHANGED,IMPRISONMENT_STATUS-CHANGED,SENTENCE_CALCULATION_DATES-CHANGED"))
                    .filter(r -> r.getStatus() == HttpStatus.OK.value())
                    .map(HttpResponse::getBody)
                    .map(this::asEvents)
                    .map(offenderEvents -> offenderEvents.stream()
                            .filter(offenderEvent -> offenderEvent.getNomisEventType().endsWith(OASYS))
                            .filter(offenderEvent -> !xtagFudgedTimestampOf(offenderEvent.getEventDatetime()).equals(lastPulled.toLocalDateTime()))
                            .collect(Collectors.toList()));

            if (maybeOffenderEvents.isPresent()) {
                final List<OffenderEvent> events = maybeOffenderEvents.get();
                log.info("Pulled {} events...", events.size());
                handleEvents(events);
                lastPulled = latestTimestampOf(maybeOffenderEvents.get());
            } else {
                log.info("No events to pull...");
            }
        } catch (Exception e) {
            log.error("Caught error in processing loop: message is {}, class {}", e.getMessage(), e.getCause().getClass());
        }
    }

    private ZonedDateTime latestTimestampOf(List<OffenderEvent> offenderEvents) {
        final LocalDateTime latestXtagEnqueueTime = offenderEvents.stream().max(Comparator.comparing(OffenderEvent::getEventDatetime)).map(OffenderEvent::getEventDatetime).orElse(null);

        return xtagFudgedTimestampOf(latestXtagEnqueueTime).atZone(LONDON);
    }

    private void handleEvents(List<OffenderEvent> events) throws ExecutionException, UnirestException, NomisAPIServiceError, JsonProcessingException, OasysAPIServiceError, RetryException {
        for (OffenderEvent offenderEvent : events) {
            sendToOAsys(xtagEventMessageOf(offenderEvent));
        }
    }

    private void sendToOAsys(Optional<EventMessage> maybeEventMessage) throws JsonProcessingException, UnirestException, OasysAPIServiceError {
        if (maybeEventMessage.isPresent()) {
            final EventMessage eventMessage = maybeEventMessage.get();
            final String oasysSoapXml = xmlMapper.writeValueAsString(eventMessage);

            messageStoreService.writeMessage(
                    oasysSoapXml,
                    eventMessage.getCorrelationId(),
                    eventMessage.getNomisId(),
                    "XTAG",
                    MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation);

            final HttpResponse<String> response = oasysSOAPClient.oasysWebServiceResponseOf(oasysSoapXml);

            if (response.getStatus() != HttpStatus.OK.value()) {
                exceptionLogService.logFault(oasysSoapXml, eventMessage.getCorrelationId(), "Bad response from oasys.");
            }
            if (response.getStatus() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                throw new OasysAPIServiceError("Can't send event message to oasys. Response " + response.getStatus());
            }
        }
    }

    private Optional<EventMessage> xtagEventMessageOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError, RetryException {

        try {
            switch (event.getNomisEventType()) {
                case "OFF_IMP_STAT_OASYS":
                    return xtagTransformer.offenderImprisonmentStatusUpdatedXtagOf(event);
                case "OFF_DISCH_OASYS":
                    return xtagTransformer.offenderDischargeXtagOf(event);
                case "OFF_RECEP_OASYS":
                    return xtagTransformer.offenderReceptionXtagOf(event);
                case "BOOK_UPD_OASYS":
                    return xtagTransformer.bookingUpdatedXtagOf(event);
                case "OFF_UPD_OASYS":
                    return xtagTransformer.offenderUpdatedXtagOf(event);
                case "OFF_SENT_OASYS":
                    return xtagTransformer.offenderSentenceUpdatedXtagOf(event);
            }
        } catch (NDHMappingException ndhme) {
            exceptionLogService.logMappingFail(ndhme.getCode(), ndhme.getValue(), ndhme.getSubject(), "n/a", anIdentifierFor(event));
        }

        return Optional.empty();
    }

    private String anIdentifierFor(OffenderEvent event) {
        return firstNonNullOf(ImmutableList.of(
                Optional.ofNullable(event.getOffenderIdDisplay()),
                Optional.ofNullable(event.getOffenderId()),
                Optional.ofNullable(event.getBookingNumber()),
                Optional.ofNullable(event.getBookingId())));
    }

    private String firstNonNullOf(ImmutableList<Optional<?>> candidates) {
        return candidates.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Object::toString)
                .findFirst().orElse("n/a");
    }


    private List<OffenderEvent> asEvents(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, OffenderEvent[].class));
        } catch (IOException e) {
            log.error("Failed to turn json {} into OffenderEvent list.", e.getMessage());
            return Collections.emptyList();
        }
    }

    private ZonedDateTime getInitialPullFromDateTime() {
        final Optional<MsgStore> maybeLatestMsg = Optional.ofNullable(messageStoreRepository.findFirstByProcessNameOrderByMsgStoreSeqDesc("XTAG").orElse(messageStoreRepository.findFirstByProcessNameOrderByMsgStoreSeqDesc("OASys-REvents").orElse(null)));

        final ZonedDateTime z = maybeLatestMsg.map(MsgStore::getMsgTimestamp)
                .map(Timestamp::toLocalDateTime)
                .map(dt -> ZonedDateTime.of(dt, ZoneId.of("Z")))
                .orElse(ZonedDateTime.now().minusDays(1L));

        log.info("Startup pulling from {} which was derived from {}", z.toString(), maybeLatestMsg.map(MsgStore::toString).orElse("time now minus one day"));
        return z;
    }
}
