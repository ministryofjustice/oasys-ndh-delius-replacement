package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.nomis.Booking;
import uk.gov.justice.digital.ndh.api.nomis.ExternalMovement;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.nomis.OffenderImprisonmentStatus;
import uk.gov.justice.digital.ndh.api.nomis.Sentence;
import uk.gov.justice.digital.ndh.api.nomis.SentenceCalculation;
import uk.gov.justice.digital.ndh.api.nomis.elite2.InmateDetail;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@Slf4j
public class XtagTransformer {

    public static final long IMPRISONMENT_STATUS_CODE_TYPE = 14L;
    public static final long AGENCY_LOCATION_CODE_TYPE = 2005L;
    public static final String OFFENDER_LIFER = "OffenderLifer";
    public static final String STATUS_CHANGE = "StatusChange";
    public static final long OASYSR_RECEPTION_CODES = 2015L;
    public static final long OASYSR_DISCHARGE_CODES = 2016L;
    private final NomisClient custodyApiClient;
    private final NomisClient elite2ApiClient;
    private final ObjectMapper objectMapper;
    private final MappingService mappingService;
    private final OffenderTransformer offenderTransformer;
    private final Sequence xtagSequence;


    public XtagTransformer(NomisClient custodyApiClient, NomisClient elite2ApiClient, @Qualifier("globalObjectMapper") ObjectMapper objectMapper, MappingService mappingService, OffenderTransformer offenderTransformer, Sequence xtagSequence) {
        this.custodyApiClient = custodyApiClient;
        this.elite2ApiClient = elite2ApiClient;
        this.objectMapper = objectMapper;
        this.mappingService = mappingService;
        this.offenderTransformer = offenderTransformer;
        this.xtagSequence = xtagSequence;
    }

    public Optional<EventMessage> offenderImprisonmentStatusUpdatedXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);

        final Optional<OffenderImprisonmentStatus> maybeOffenderImprisonmentStatus = custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/imprisonmentStatuses")
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asImprisonmentStatuses)
                .flatMap(imprisonmentStatuses -> imprisonmentStatuses.stream()
                        .filter(imprisonmentStatus -> imprisonmentStatus.getImprisonmentStatus().getImprisonmentStatusSeq().equals(event.getImprisonmentStatusSeq()))
                        .filter(OffenderImprisonmentStatus::getLatestStatus)
                        .findFirst());

        final OffenderImprisonmentStatus offenderImprisonmentStatus = maybeOffenderImprisonmentStatus.orElseThrow(() -> new NomisAPIServiceError("Can't get offender imprisonment statuses."));

        return Optional.of(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(this.pncOf(offender))
                .nomisId(inmateDetail.getOffenderNo())
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .eventType(this.statusEventTypeOf(offenderImprisonmentStatus))
                .establishmentCode(this.establishmentCodeOf(offenderImprisonmentStatus))
                .dateOfBirth(offender.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build()).filter(eventMessage -> OFFENDER_LIFER.equals(eventMessage.getEventType()));
    }

    public Offender getOffender(InmateDetail inmateDetail) throws NomisAPIServiceError, UnirestException, ExecutionException {
        return custodyApiClient
                .doGetWithRetry("offenders/nomsId/" + inmateDetail.getOffenderNo())
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(offenderTransformer::asOffender).orElseThrow(() -> new NomisAPIServiceError("Can't get offender detail."));
    }

    public SentenceCalculation getSentenceCalculation(Offender offender, InmateDetail inmateDetail) throws NomisAPIServiceError, UnirestException, ExecutionException {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentenceCalculations", ImmutableMap.of("bookingId", inmateDetail.getBookingId()))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asSentenceCalculations)
                .flatMap(sentences -> sentences.stream().findFirst()).orElseThrow(() -> new NomisAPIServiceError("Can't get offender sentence calculations."));
    }

    public ExternalMovement getMovement(Offender offender, InmateDetail inmateDetail, OffenderEvent offenderEvent) throws NomisAPIServiceError, UnirestException, ExecutionException {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/movements", ImmutableMap.of("bookingId", inmateDetail.getBookingId()))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asExternalMovements)
                .flatMap(externalMovements -> externalMovements.stream()
                        .filter(movement -> movement.getSequenceNumber().equals(offenderEvent.getMovementSeq()))
                        .findFirst()).orElseThrow(() -> new NomisAPIServiceError("Can't get offender movements."));
    }

    private List<ExternalMovement> asExternalMovements(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, ExternalMovement[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<SentenceCalculation> asSentenceCalculations(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, SentenceCalculation[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public InmateDetail getInmateDetail(OffenderEvent event) throws UnirestException, ExecutionException, NomisAPIServiceError {
        return elite2ApiClient
                .doGetWithRetry("bookings/" + event.getBookingId())
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asInmateDetail).orElseThrow(() -> new NomisAPIServiceError("Can't get inmate detail."));
    }

    private String establishmentCodeOf(OffenderImprisonmentStatus offenderImprisonmentStatus) {
        return mappingService.targetValueOf(offenderImprisonmentStatus.getAgencyLocationId(), AGENCY_LOCATION_CODE_TYPE);
    }

    private List<OffenderImprisonmentStatus> asImprisonmentStatuses(String jsonStr) {
        try {
            final OffenderImprisonmentStatus[] imprisonmentStatuses = objectMapper.readValue(jsonStr, OffenderImprisonmentStatus[].class);
            return Arrays.asList(imprisonmentStatuses);
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private String statusEventTypeOf(OffenderImprisonmentStatus offenderImprisonmentStatus) {
        final Long imprisonmentStatusNumeric = mappingService.numeric1Of(offenderImprisonmentStatus.getImprisonmentStatus().getImprisonmentStatus(), IMPRISONMENT_STATUS_CODE_TYPE);
        return Long.valueOf(1).equals(imprisonmentStatusNumeric) ? OFFENDER_LIFER : STATUS_CHANGE;
    }

    private String pncOf(Offender offender) {
        return Optional.ofNullable(offender.getIdentifiers())
                .flatMap(identifiers -> identifiers.stream()
                        .filter(identifier -> "PNC".equals(identifier.getIdentifierType()))
                        .findFirst()).map(Identifier::getIdentifier)
                .map(this::normalisedPncOf)
                .orElse(null);
    }

    public String normalisedPncOf(String pnc) {
        return Optional.ofNullable(pnc)
                .filter(s -> s.indexOf("/") == 5)
                .map(s -> s.substring(2))
                .orElse(pnc);
    }

    private InmateDetail asInmateDetail(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, InmateDetail.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public Optional<EventMessage> offenderReceptionXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);
        final List<Sentence> activeSentences = getActiveSentences(offender, inmateDetail);
        final SentenceCalculation sentenceCalculation = getSentenceCalculation(offender, inmateDetail);
        final ExternalMovement offenderMovement = getMovement(offender, inmateDetail, event);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceMonths(monthsOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceDays(daysOf(sentenceCalculation.getEffectiveSentenceLength()))
                .releaseDate(sentenceCalculation.getReleaseDate().toString())
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(offender))
                .nomisId(offender.getNomsId())
                .movementFromTo(receptionMovementFromToOf(offenderMovement))
                .movementDelete("N")
                .movementCourtCode(receptionMovementCourtCodeOf(offenderMovement))
                .movementCode(receptionMovementCodeOf(offenderMovement.getMovementReasonCode()))
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .eventType("OffenderReception")
                .establishmentCode(establishmentCodeOf(offenderMovement, offender))
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, sentenceCalculation))
                .dateOfBirth(offender.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build());
    }

    private String receptionMovementCodeOf(String movementReasonCode) {
        return mappingService.targetValueOf(movementReasonCode, OASYSR_RECEPTION_CODES);
    }

    private String receptionMovementCourtCodeOf(ExternalMovement offenderMovement) {
        return ("CRT".equals(offenderMovement.getMovementTypeCode())) ? offenderMovement.getFromAgencyLocationId() : null;
    }


    public Optional<EventMessage> bookingUpdatedXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(offender))
                .oldPrisonNumber(event.getPreviousBookingNumber())
                .nomisId(offender.getNomsId())
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .establishmentCode(establishmentCodeOf(null, offender))
                .eventType("OffenderPrisonNumber")
                .correlationId(nextCorrelationId())
                .build());
    }

    public Optional<EventMessage> offenderDischargeXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {

        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);
        final List<Sentence> activeSentences = getActiveSentences(offender, inmateDetail);
        final SentenceCalculation sentenceCalculation = getSentenceCalculation(offender, inmateDetail);
        final ExternalMovement offenderMovement = getMovement(offender, inmateDetail, event);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceMonths(monthsOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceDays(daysOf(sentenceCalculation.getEffectiveSentenceLength()))
                .releaseDate(sentenceCalculation.getReleaseDate().toString())
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(offender))
                .nomisId(offender.getNomsId())
                .movementFromTo(dischargeMovementFromToOf(offenderMovement))
                .movementDelete("N")
                .movementCode(dischargeMovementCodeOf(offenderMovement.getMovementReasonCode()))
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .dateOfBirth(offender.getDateOfBirth().toString())
                .establishmentCode(establishmentCodeOf(offenderMovement, offender))
                .eventType("OffenderDischarge")
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, sentenceCalculation))
                .correlationId(nextCorrelationId())
                .build());
    }

    private String dischargeMovementCodeOf(String movementReasonCode) {
        return mappingService.targetValueOf(movementReasonCode, OASYSR_DISCHARGE_CODES);
    }

    private String nextCorrelationId() {
        return "NOMISHNOMIS" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                xtagSequence.nextVal();
    }


    private String establishmentCodeOf(ExternalMovement offenderMovement, Offender offender) {
        return mappingService.targetValueOf(Optional.ofNullable(offenderMovement)
                .filter(om -> "OUT".equals(om.getMovementDirection()))
                .map(ExternalMovement::getFromAgencyLocationId)
                .orElse(activeBookingOf(offender).getAgencyLocation().getAgencyLocationId()), AGENCY_LOCATION_CODE_TYPE);
    }

    private Booking activeBookingOf(Offender offender) {
        return offender.getBookings().stream()
                .filter(Booking::getActiveFlag)
                .findFirst()
                .orElse(null);
    }

    private String effectiveSentenceLengthOf(List<Sentence> activeSentences, SentenceCalculation sentenceCalculation) {
        return activeSentences.stream()
                .min(Comparator.comparing(Sentence::getStartDate))
                .map(Sentence::getStartDate)
                .map(startDate -> Period.between(startDate, sentenceCalculation.getEffectiveSentenceEndDate().toLocalDate()).getDays() + 1)
                .map(String::valueOf)
                .orElse(null);
    }

    private List<Sentence> getActiveSentences(Offender offender, InmateDetail inmateDetail) throws ExecutionException, UnirestException, NomisAPIServiceError {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentences", ImmutableMap.of("bookingId", inmateDetail.getBookingId()))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asSentences)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender sentence calculations."));
    }

    private List<Sentence> asSentences(String jsonStr) {
        try {
            final Sentence[] sentences = objectMapper.readValue(jsonStr, Sentence[].class);
            return Arrays.asList(sentences).stream()
                    .filter(Sentence::getIsActive)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

    }

    private String dischargeMovementFromToOf(ExternalMovement offenderMovement) {
        return "CRT".equals(mappingService.targetValueOf(offenderMovement.getMovementTypeCode(), OASYSR_DISCHARGE_CODES)) ? null : offenderMovement.getToAgencyLocationId();

    }

    private String receptionMovementFromToOf(ExternalMovement offenderMovement) {
        return "CRT".equals(mappingService.targetValueOf(offenderMovement.getMovementTypeCode(), OASYSR_RECEPTION_CODES)) ? null : offenderMovement.getFromAgencyLocationId();
    }

    private String oasysTimestampOf(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSS"));
    }

    private String yearsOf(String effectiveSentenceLength) {
        return Optional.ofNullable(effectiveSentenceLength).map(
                esl -> Integer.valueOf(esl.split("/")[0]).toString()).orElse(null);
    }

    private String monthsOf(String effectiveSentenceLength) {
        return Optional.ofNullable(effectiveSentenceLength).map(
                esl -> Integer.valueOf(esl.split("/")[1]).toString()).orElse(null);
    }

    private String daysOf(String effectiveSentenceLength) {
        return Optional.ofNullable(effectiveSentenceLength).map(
                esl -> Integer.valueOf(esl.split("/")[2]).toString()).orElse(null);
    }

    public Optional<EventMessage> offenderUpdatedXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(offender))
                .oldPrisonNumber(event.getPreviousBookingNumber())
                .nomisId(offender.getNomsId())
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .eventType("OffenderDetails")
                .establishmentCode(establishmentCodeOf(null, offender))
                .dateOfBirth(offender.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build());
    }

    public Optional<EventMessage> offenderSentenceUpdatedXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);
        final List<Sentence> activeSentences = getActiveSentences(offender, inmateDetail);
        final SentenceCalculation sentenceCalculation = getSentenceCalculation(offender, inmateDetail);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceMonths(monthsOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceDays(daysOf(sentenceCalculation.getEffectiveSentenceLength()))
                .sentenceDate(sentenceStartDateOf(activeSentences))
                .releaseDate(sentenceCalculation.getReleaseDate().toString())
                .pnc(pncOf(offender))
                .nomisId(offender.getNomsId())
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .eventType("OffenderSentence")
                .establishmentCode(establishmentCodeOf(null, offender))
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, sentenceCalculation))
                .dateOfBirth(offender.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build());
    }

    private String sentenceStartDateOf(List<Sentence> activeSentences) {
        return activeSentences.stream()
                .min(Comparator.comparing(Sentence::getStartDate))
                .map(Sentence::getStartDate)
                .map(LocalDate::toString)
                .orElse(null);
    }
}