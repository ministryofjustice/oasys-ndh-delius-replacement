package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.rholder.retry.RetryException;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.nomis.AgencyLocation;
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
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static String normalisedPncOf(String pnc) {
        return Optional.ofNullable(pnc)
                .filter(s -> s.indexOf("/") == 4)
                .map(s -> s.substring(2))
                .orElse(pnc);
    }

    public static String pncOf(Offender offender) {

        final Stream<Identifier> identifierStream1 = Optional.ofNullable(offender.getIdentifiers())
                .stream()
                .flatMap(Collection::stream);
        final Stream<Identifier> identifierStream2 = Optional.ofNullable(offender.getAliases())
                .stream()
                .flatMap(Collection::stream)
                .flatMap(a -> Optional.ofNullable(a.getIdentifiers()).stream().flatMap(Collection::stream));

        return Stream.concat(identifierStream1, identifierStream2)
                .filter(identifier -> "PNC".equals(identifier.getIdentifierType()))
                .findFirst().map(Identifier::getIdentifier)
                .map(XtagTransformer::normalisedPncOf)
                .orElse(null);
    }

    public Optional<EventMessage> offenderImprisonmentStatusUpdatedXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {

        log.info("Handling offenderImprisonmentStatusUpdated event {}", event);
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender rootOffender = getOffender(inmateDetail);

        log.info("... which is for nomsId {}", inmateDetail.getOffenderNo());

        final Optional<OffenderImprisonmentStatus> maybeOffenderImprisonmentStatus = custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + rootOffender.getOffenderId() + "/imprisonmentStatuses")
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asImprisonmentStatuses)
                .flatMap(imprisonmentStatuses -> imprisonmentStatuses.stream()
                        .filter(imprisonmentStatus -> (event.getImprisonmentStatusSeq() == null) || event.getImprisonmentStatusSeq().equals(imprisonmentStatus.getImprisonmentStatus().getImprisonmentStatusSeq()))
                        .filter(OffenderImprisonmentStatus::getLatestStatus)
                        .findFirst());

        final OffenderImprisonmentStatus offenderImprisonmentStatus = maybeOffenderImprisonmentStatus.orElseThrow(() -> new NomisAPIServiceError("Can't get offender imprisonment statuses."));

        return Optional.of(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(inmateDetail.getOffenderNo())
                .forename1(inmateDetail.getFirstName())
                .forename2(inmateDetail.getMiddleName())
                .familyName(inmateDetail.getLastName())
                .eventType(this.statusEventTypeOf(offenderImprisonmentStatus))
                .establishmentCode(this.establishmentCodeOf(offenderImprisonmentStatus))
                .dateOfBirth(inmateDetail.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build())
                .filter(eventMessage -> {
                    final boolean lifer = OFFENDER_LIFER.equals(eventMessage.getEventType());
                    log.info("Is Lifer? {}", lifer);
                    return lifer;
                });
    }

    public Offender getOffender(InmateDetail inmateDetail) throws NomisAPIServiceError, ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("offenders/nomsId/" + inmateDetail.getOffenderNo())
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(offenderTransformer::asOffender).orElseThrow(() -> new NomisAPIServiceError("Can't get offender detail."));
    }

    public Optional<SentenceCalculation> getSentenceCalculation(Offender offender, Long bookingId) throws ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentenceCalculations", ImmutableMap.of("bookingId", bookingId))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asSentenceCalculations)
                .flatMap(sentences -> sentences.stream().findFirst());
    }

    public ExternalMovement getAdmissionMovementFallback(Long bookingId) throws NomisAPIServiceError, ExecutionException, RetryException {
        return getExternalMovements(bookingId)
                .flatMap(externalMovements -> externalMovements.stream()
                        .filter(movement -> "ADM".equals(movement.getMovementTypeCode()))
                        .findFirst()).orElseThrow(() -> new NomisAPIServiceError("Can't get admission movement ADM fallback for booking " + bookingId));
    }

    public Optional<List<ExternalMovement>> getExternalMovements(Long bookingId) throws ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("/movements", ImmutableMap.of("bookingId", bookingId, "size", 2000))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asPagedExternalMovements);
    }

    private List<SentenceCalculation> asSentenceCalculations(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, SentenceCalculation[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public InmateDetail getInmateDetail(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        return elite2ApiClient
                .doGetWithRetry("bookings/" + event.getBookingId(), ImmutableMap.of("basicInfo","true"))
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

    private InmateDetail asInmateDetail(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, InmateDetail.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public Optional<EventMessage> offenderReceptionXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        log.info("Handling offenderReception event {}", event);
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender rootOffender = getOffender(inmateDetail);
        log.info("... which is for nomsId {}", inmateDetail.getOffenderNo());
        final List<Sentence> activeSentences = getActiveSentences(rootOffender, inmateDetail.getBookingId());
        final Optional<SentenceCalculation> maybeSentenceCalculation = getSentenceCalculation(rootOffender, inmateDetail.getBookingId());
        final ExternalMovement offenderMovement = getAdmissionMovement(event);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(maybeSentenceCalculation))
                .sentenceMonths(monthsOf(maybeSentenceCalculation))
                .sentenceDays(daysOf(maybeSentenceCalculation))
                .releaseDate(safeReleaseDateOf(maybeSentenceCalculation))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(rootOffender.getNomsId())
                .movementFromTo(receptionMovementFromToOf(offenderMovement))
                .movementDelete("N")
                .movementCourtCode(receptionMovementCourtCodeOf(offenderMovement))
                .movementCode(receptionMovementCodeOf(offenderMovement.getMovementReasonCode()))
                .forename1(inmateDetail.getFirstName())
                .forename2(inmateDetail.getMiddleName())
                .familyName(inmateDetail.getLastName())
                .eventType("OffenderReception")
                .establishmentCode(establishmentCodeOf(offenderMovement, rootOffender))
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, maybeSentenceCalculation))
                .dateOfBirth(inmateDetail.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build());
    }

    private ExternalMovement getAdmissionMovement(OffenderEvent event) throws ExecutionException, RetryException, NomisAPIServiceError {
        if (event.getMovementSeq() != null) {
            return getExternalMovement(event);
        } else {
            log.warn("Admission movement event {} does not have a movement sequence. Doing fallback by moveent type code ADM.", event);
            return getAdmissionMovementFallback(event.getBookingId());
        }
    }

    private ExternalMovement getExternalMovement(OffenderEvent event) throws NomisAPIServiceError, ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("movements/bookingId/" + event.getBookingId() + "/sequence/" + event.getMovementSeq())
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asExternalMovement)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender movement for bookingId " + event.getBookingId() + " and sequence " + event.getMovementSeq() + "."));
    }

    public String safeReleaseDateOf(Optional<SentenceCalculation> maybeSentenceCalculation) {
        return maybeSentenceCalculation.flatMap(sc -> Optional.ofNullable(sc.getReleaseDate())).map(LocalDate::toString).orElse(null);
    }

    public String receptionMovementCodeOf(String movementReasonCode) {
        try {
            return mappingService.targetValueOf(movementReasonCode, OASYSR_RECEPTION_CODES);
        } catch (NDHMappingException ndhme) {
            log.warn("Failed to lookup reception movement code {} in group {}. Trying discharge code instead...", movementReasonCode, OASYSR_RECEPTION_CODES);
        }

        dischargeMovementCodeOf(movementReasonCode);

        return "R";
    }

    private String receptionMovementCourtCodeOf(ExternalMovement offenderMovement) {
        return ("CRT".equals(offenderMovement.getMovementTypeCode())) ? offenderMovement.getFromAgencyLocation().getAgencyLocationId() : null;
    }


    public Optional<EventMessage> bookingUpdatedXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        log.info("Handling bookingUpdated event {}", event);
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender rootOffender = getOffender(inmateDetail);
        log.info("... which is for nomsId {}", inmateDetail.getOffenderNo());

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .oldPrisonNumber(event.getPreviousBookingNumber())
                .nomisId(rootOffender.getNomsId())
                .forename1(inmateDetail.getFirstName())
                .forename2(inmateDetail.getMiddleName())
                .familyName(inmateDetail.getLastName())
                .establishmentCode(establishmentCodeOf(null, rootOffender))
                .dateOfBirth(inmateDetail.getDateOfBirth().toString())
                .eventType("OffenderPrisonNumber")
                .correlationId(nextCorrelationId())
                .build());
    }

    public Optional<EventMessage> offenderDischargeXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        log.info("Handling offenderDischarge event {}", event);
        final ExternalMovement offenderMovement = getDischargeMovement(event);
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender rootOffender = getOffender(inmateDetail);
        log.info("... which is for nomsId {}", inmateDetail.getOffenderNo());


        final List<Sentence> activeSentences = getActiveSentences(rootOffender, event.getBookingId());
        final Optional<SentenceCalculation> maybeSentenceCalculation = getSentenceCalculation(rootOffender, event.getBookingId());

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(maybeSentenceCalculation))
                .sentenceMonths(monthsOf(maybeSentenceCalculation))
                .sentenceDays(daysOf(maybeSentenceCalculation))
                .releaseDate(safeReleaseDateOf(maybeSentenceCalculation))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(inmateDetail.getOffenderNo())
                .movementFromTo(dischargeMovementFromToOf(offenderMovement))
                .movementCourtCode(dischargeMovementCourtCodeOf(offenderMovement))
                .movementDelete("N")
                .movementCode(dischargeMovementCodeOf(offenderMovement.getMovementReasonCode()))
                .forename1(inmateDetail.getFirstName())
                .forename2(inmateDetail.getMiddleName())
                .familyName(inmateDetail.getLastName())
                .dateOfBirth(inmateDetail.getDateOfBirth().toString())
                .establishmentCode(establishmentCodeOf(offenderMovement, rootOffender))
                .eventType("OffenderDischarge")
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, maybeSentenceCalculation))
                .correlationId(nextCorrelationId())
                .build());
    }

    private ExternalMovement getDischargeMovement(OffenderEvent event) throws ExecutionException, RetryException, NomisAPIServiceError {

        if (event.getMovementSeq() != null) {
            return getExternalMovement(event);
        } else {
            log.warn("Discharge movement event {} does not have a movement sequence. Doing fallback by code type TRN", event);
            return getDischargeMovementFallback(event.getBookingId());
        }
    }

    private ExternalMovement asExternalMovement(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, ExternalMovement.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private String dischargeMovementCourtCodeOf(ExternalMovement offenderMovement) {
        final AgencyLocation toAgencyLocation = offenderMovement.getToAgencyLocation();

        if (toAgencyLocation == null) {
            log.info("Offender movement has no 'toAgencyLocation': {}", offenderMovement);
            return null;
        }

        return "CRT".equals(offenderMovement.getMovementTypeCode()) ? agencyOrOutOf(toAgencyLocation.getAgencyLocationId()) : null;
    }

    private ExternalMovement getDischargeMovementFallback(Long bookingId) throws ExecutionException, RetryException, NomisAPIServiceError {
        return getExternalMovements(bookingId)
                .flatMap(ems -> ems.stream()
                        .filter(em -> "TRN".equals(em.getMovementTypeCode()))
                        .findFirst())
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender TRN movement for bookingId " + bookingId));
    }

    private List<ExternalMovement> asPagedExternalMovements(String s) {
        try {
            final JsonNode jsonNode = objectMapper.readTree(s);
            ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ExternalMovement>>() {
            });
            return reader.readValue(jsonNode.at("/_embedded/externalMovementList"));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
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
        return mappingService.targetValueOf(
                Optional.ofNullable(offenderMovement)
                        .filter(om -> "OUT".equals(om.getMovementDirection()))
                        .map(x -> x.getFromAgencyLocation().getAgencyLocationId())
                        .orElse(activeBookingOf(offender)
                                .map(booking -> booking.getAgencyLocation().getAgencyLocationId())
                                .orElse(null)),
                AGENCY_LOCATION_CODE_TYPE);

    }

    private Optional<Booking> activeBookingOf(Offender offender) {
        return offender.getBookings().stream()
                .filter(Booking::getActiveFlag)
                .findFirst();
    }

    private String effectiveSentenceLengthOf(List<Sentence> activeSentences, Optional<SentenceCalculation> maybeSentenceCalculation) {

        return maybeSentenceCalculation
                .filter(sentenceCalculation -> sentenceCalculation.getEffectiveSentenceEndDate() != null)
                .map(sentenceCalculation -> activeSentences.stream()
                        .min(Comparator.comparing(Sentence::getStartDate))
                        .map(Sentence::getStartDate)
                        .map(startDate -> ChronoUnit.DAYS.between(startDate, sentenceCalculation.getEffectiveSentenceEndDate().toLocalDate()) + 1)
                        .map(String::valueOf)
                        .orElse(null))
                .orElse(null);

    }

    private List<Sentence> getActiveSentences(Offender offender, Long bookingId) throws ExecutionException, NomisAPIServiceError, RetryException {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentences", ImmutableMap.of("bookingId", bookingId))
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asSentences)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender sentence calculations."));
    }

    private List<Sentence> asSentences(String jsonStr) {
        try {
            final Sentence[] sentences = objectMapper.readValue(jsonStr, Sentence[].class);
            return Arrays.stream(sentences)
                    .filter(Sentence::getIsActive)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

    }

    private String dischargeMovementFromToOf(ExternalMovement offenderMovement) {
        final AgencyLocation toAgencyLocation = offenderMovement.getToAgencyLocation();

        if (toAgencyLocation == null) {
            log.info("Offender movement has no 'toAgencyLocation': {}", offenderMovement);
            return null;
        }

        return "CRT".equals(offenderMovement.getMovementTypeCode()) ? null : agencyOrOutOf(toAgencyLocation.getAgencyLocationId());

    }

    private String receptionMovementFromToOf(ExternalMovement offenderMovement) {
        final AgencyLocation fromAgencyLocation = offenderMovement.getFromAgencyLocation();

        if (fromAgencyLocation == null) {
            log.info("Offender movement has no 'fromAgencyLocation': {}", offenderMovement);
            return null;
        }

        return "CRT".equals(offenderMovement.getMovementTypeCode()) ? null : agencyOrInOf(fromAgencyLocation.getAgencyLocationId());


    }

    private String agencyOrInOf(String anAgencyLocationId) {
        return Optional.ofNullable(anAgencyLocationId).orElse("IN");
    }

    private String agencyOrOutOf(String anAgencyLocationId) {
        return Optional.ofNullable(anAgencyLocationId).orElse("OUT");
    }

    private String oasysTimestampOf(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSS"));
    }

    private String yearsOf(Optional<SentenceCalculation> maybeSentenceCalculation) {
        return maybeSentenceCalculation
                .filter(sc -> sc.getReleaseDate() != null)
                .map(SentenceCalculation::getEffectiveSentenceLength)
                .map(esl -> Integer.valueOf(esl.split("/")[0]).toString())
                .orElse(null);
    }

    private String monthsOf(Optional<SentenceCalculation> maybeSentenceCalculation) {
        return maybeSentenceCalculation
                .filter(sc -> sc.getReleaseDate() != null)
                .map(SentenceCalculation::getEffectiveSentenceLength)
                .map(esl -> Integer.valueOf(esl.split("/")[1]).toString())
                .orElse(null);
    }

    private String daysOf(Optional<SentenceCalculation> maybeSentenceCalculation) {
        return maybeSentenceCalculation
                .filter(sc -> sc.getReleaseDate() != null)
                .map(SentenceCalculation::getEffectiveSentenceLength)
                .map(esl -> Integer.valueOf(esl.split("/")[2]).toString())
                .orElse(null);
    }

    public Optional<EventMessage> offenderUpdatedXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        log.info("Handling offenderUpdated event {}", event);
        final OffenderEvent eventWithBookingId = eventWithBookingIdOf(event);
        final InmateDetail inmateDetail = getInmateDetail(eventWithBookingId);
        final Offender rootOffender = getOffender(inmateDetail);
        log.info("... which is for nomsId {}", inmateDetail.getOffenderNo());


        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .oldPrisonNumber(event.getPreviousBookingNumber())
                .nomisId(inmateDetail.getOffenderNo())
                .forename1(inmateDetail.getFirstName())
                .forename2(inmateDetail.getMiddleName())
                .familyName(inmateDetail.getLastName())
                .eventType("OffenderDetails")
                .establishmentCode(establishmentCodeOf(null, rootOffender))
                .dateOfBirth(inmateDetail.getDateOfBirth().toString())
                .correlationId(nextCorrelationId())
                .build());
    }

    private OffenderEvent eventWithBookingIdOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {

        if (event.getBookingId() == null) {
            final Offender offender = custodyApiClient
                    .doGetWithRetry("offenders/offenderId/" + event.getRootOffenderId())
                    .filter(r -> r.getStatus() == HttpStatus.OK.value())
                    .map(HttpResponse::getBody)
                    .map(offenderTransformer::asOffender)
                    .orElseThrow(() -> new NomisAPIServiceError("Can't get offender " + event.getRootOffenderId()));

            final Long bookingId = offender.getBookings().stream().findFirst().map(Booking::getBookingId).orElse(null);
            return event.toBuilder().bookingId(bookingId).build();
        }

        return event;
    }

    public Optional<EventMessage> offenderSentenceUpdatedXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        log.info("Handling offenderSentenceUpdated event {}", event);
        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender rootOffender = getOffender(inmateDetail);
        log.info("... which is for nomsId {}", inmateDetail.getOffenderNo());
        final List<Sentence> activeSentences = getActiveSentences(rootOffender, inmateDetail.getBookingId());
        final Optional<SentenceCalculation> maybeSentenceCalculation = getSentenceCalculation(rootOffender, inmateDetail.getBookingId());

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(maybeSentenceCalculation))
                .sentenceMonths(monthsOf(maybeSentenceCalculation))
                .sentenceDays(daysOf(maybeSentenceCalculation))
                .sentenceDate(sentenceStartDateOf(activeSentences))
                .releaseDate(safeReleaseDateOf(maybeSentenceCalculation))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(inmateDetail.getOffenderNo())
                .forename1(inmateDetail.getFirstName())
                .forename2(inmateDetail.getMiddleName())
                .familyName(inmateDetail.getLastName())
                .eventType("OffenderSentence")
                .establishmentCode(establishmentCodeOf(null, rootOffender))
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, maybeSentenceCalculation))
                .dateOfBirth(inmateDetail.getDateOfBirth().toString())
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