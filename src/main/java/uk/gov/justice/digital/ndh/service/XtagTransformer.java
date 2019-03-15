package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.rholder.retry.RetryException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    final NomisApiServices nomisApiServices;
    private final ObjectMapper objectMapper;
    private final MappingService mappingService;
    private final CorrelationService correlationService;

    @Autowired
    public XtagTransformer(@Qualifier("globalObjectMapper") ObjectMapper objectMapper,
                           MappingService mappingService,
                           NomisApiServices nomisApiServices,
                           CorrelationService correlationService) {
        this.objectMapper = objectMapper;
        this.mappingService = mappingService;
        this.correlationService = correlationService;
        this.nomisApiServices = nomisApiServices;
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

        /*
        The OFF_IMP_STAT_OASYS raw xtag event only ever contains a booking Id. The Elite2 API uses cacheing on bookings.
        Work around the cacheing...
         */
        log.info("Handling offenderImprisonmentStatusUpdated event {}", event);
        final InmateDetail inmateDetail = nomisApiServices.getInmateDetail(event, this);
        final Offender rootOffender = nomisApiServices.getOffenderByNomsId(inmateDetail.getOffenderNo());
        final Offender thisOffender = thisOffenderFromRootOffender(rootOffender);
        log.info("... which is for nomsId {}", thisOffender.getNomsId());

        final Optional<OffenderImprisonmentStatus> maybeOffenderImprisonmentStatus =
                nomisApiServices.getImprisonmentStatuses(rootOffender, event, this)
                        .stream()
                        .filter(imprisonmentStatus -> (event.getImprisonmentStatusSeq() == null) || event.getImprisonmentStatusSeq().equals(imprisonmentStatus.getImprisonmentStatus().getImprisonmentStatusSeq()))
                        .filter(OffenderImprisonmentStatus::getLatestStatus)
                        .findFirst();

        final OffenderImprisonmentStatus offenderImprisonmentStatus = maybeOffenderImprisonmentStatus.orElseThrow(() -> new NomisAPIServiceError("Can't get offender imprisonment statuses."));

        return Optional.of(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(thisOffender.getNomsId())
                .forename1(thisOffender.getFirstName())
                .forename2(thisOffender.getMiddleNames())
                .familyName(thisOffender.getSurname())
                .eventType(this.statusEventTypeOf(offenderImprisonmentStatus))
                .establishmentCode(this.establishmentCodeOf(offenderImprisonmentStatus))
                .dateOfBirth(thisOffender.getDateOfBirth().toString())
                .correlationId(correlationService.nextCorrelationId())
                .build())
                .filter(eventMessage -> {
                    final boolean lifer = OFFENDER_LIFER.equals(eventMessage.getEventType());
                    log.info("Is Lifer? {}", lifer);
                    return lifer;
                });
    }

    public Offender thisOffenderFromRootOffender(Offender rootOffender) throws ExecutionException, RetryException, NomisAPIServiceError {

        final Long bookingOffenderId = bookingOffenderIdOf(rootOffender);
        if (bookingOffenderId.equals(rootOffender.getOffenderId())) {
            return rootOffender;
        }

        return nomisApiServices.getOffenderByOffenderId(bookingOffenderId);
    }

    private Long bookingOffenderIdOf(Offender rootOffender) {
        return rootOffender.getBookings().stream().findFirst().map(Booking::getOffenderId).orElse(null);
    }

    public ExternalMovement getAdmissionMovementFallback(Long bookingId) throws NomisAPIServiceError, ExecutionException, RetryException {
        return nomisApiServices.getExternalMovements(bookingId, this)
                .flatMap(externalMovements -> externalMovements.stream()
                        .filter(movement -> "ADM".equals(movement.getMovementTypeCode()))
                        .findFirst()).orElseThrow(() -> new NomisAPIServiceError("Can't get admission movement ADM fallback for booking " + bookingId));
    }

    public List<SentenceCalculation> asSentenceCalculations(String jsonStr) {
        if (Strings.isNullOrEmpty(jsonStr)) {
            return null;
        }

        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, SentenceCalculation[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private String establishmentCodeOf(OffenderImprisonmentStatus offenderImprisonmentStatus) {
        return mappingService.targetValueOf(offenderImprisonmentStatus.getAgencyLocationId(), AGENCY_LOCATION_CODE_TYPE);
    }

    public List<OffenderImprisonmentStatus> asImprisonmentStatuses(String jsonStr) {
        if (Strings.isNullOrEmpty(jsonStr)) {
            return null;
        }

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

    public InmateDetail asInmateDetail(String jsonStr) {
        if (Strings.isNullOrEmpty(jsonStr)) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonStr, InmateDetail.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public Optional<EventMessage> offenderReceptionXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        /*
        The OFF_RECEP_OASYS raw xtag event only ever contains a booking Id. The Elite2 API uses cacheing on bookings.
        Work around the cacheing...
         */
        log.info("Handling offenderReception event {}", event);
        final InmateDetail inmateDetail = nomisApiServices.getInmateDetail(event, this);
        final Offender rootOffender = nomisApiServices.getOffenderByNomsId(inmateDetail.getOffenderNo());
        final Offender thisOffender = thisOffenderFromRootOffender(rootOffender);
        log.info("... which is for nomsId {}", thisOffender.getNomsId());

        final Long bookingId = event.getBookingId();
        final List<Sentence> activeSentences = nomisApiServices.getActiveSentences(rootOffender, bookingId, this);
        final Optional<SentenceCalculation> maybeSentenceCalculation = nomisApiServices.getSentenceCalculation(rootOffender, bookingId, this);
        final ExternalMovement offenderMovement = nomisApiServices.getAdmissionMovement(event, this);

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
                .forename1(thisOffender.getFirstName())
                .forename2(thisOffender.getMiddleNames())
                .familyName(thisOffender.getSurname())
                .eventType("OffenderReception")
                .establishmentCode(establishmentCodeOf(offenderMovement, rootOffender))
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, maybeSentenceCalculation))
                .dateOfBirth(thisOffender.getDateOfBirth().toString())
                .correlationId(correlationService.nextCorrelationId())
                .build());
    }

    public String safeReleaseDateOf(Optional<SentenceCalculation> maybeSentenceCalculation) {
        return maybeSentenceCalculation.flatMap(sc -> Optional.ofNullable(sc.getReleaseDate())).map(LocalDate::toString).orElse(null);
    }

    public String receptionMovementCodeOf(String movementReasonCode) {
        try {
            return mappingService.targetValueOf(movementReasonCode, OASYSR_RECEPTION_CODES, false);
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

        // BOOK_UPD_OASYS will always contain the new offender Id

        final Offender thisOffender = nomisApiServices.getOffenderByOffenderId(event.getOffenderId());

        log.info("... which is for nomsId {}", thisOffender.getNomsId());

        // Not sure if thisOffender will necessarily be a root offender but safest not to assume.
        // The following being absent is a good indicator that we are not dealing with root offender.
        String pnc = pncOf(thisOffender);
        String prisonNumber = bookingNoOf(thisOffender);
        String establishmentCode = establishmentCodeOf(null, thisOffender);

        if (pnc == null || prisonNumber == null || establishmentCode == null) {
            final Offender rootOffender = nomisApiServices.getOffenderByNomsId(thisOffender.getNomsId());
            pnc = pncOf(rootOffender);
            prisonNumber = bookingNoOf(rootOffender);
            establishmentCode = establishmentCodeOf(null, rootOffender);
        }

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(prisonNumber)
                .pnc(pnc)
                .oldPrisonNumber(event.getPreviousBookingNumber())
                .nomisId(thisOffender.getNomsId())
                .forename1(thisOffender.getFirstName())
                .forename2(thisOffender.getMiddleNames())
                .familyName(thisOffender.getSurname())
                .establishmentCode(establishmentCode)
                .dateOfBirth(thisOffender.getDateOfBirth().toString())
                .eventType("OffenderPrisonNumber")
                .correlationId(correlationService.nextCorrelationId())
                .build());
    }

    public String bookingNoOf(Offender rootOffender) {
        return Optional.ofNullable(rootOffender.getBookings())
                .flatMap(bookings -> bookings.stream().findFirst())
                .map(Booking::getBookingNo).orElse(null);
    }

    public Optional<EventMessage> offenderDischargeXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        // OFF_DISCH_OASYS always contains booking id
        log.info("Handling offenderDischarge event {}", event);
        final ExternalMovement offenderMovement = nomisApiServices.getDischargeMovement(event, this);
        final InmateDetail inmateDetail = nomisApiServices.getInmateDetail(event, this);
        final Offender rootOffender = nomisApiServices.getOffenderByNomsId(inmateDetail.getOffenderNo());
        final Offender thisOffender = thisOffenderFromRootOffender(rootOffender);
        log.info("... which is for nomsId {}", thisOffender.getNomsId());


        final List<Sentence> activeSentences = nomisApiServices.getActiveSentences(rootOffender, event.getBookingId(), this);
        final Optional<SentenceCalculation> maybeSentenceCalculation = nomisApiServices.getSentenceCalculation(rootOffender, event.getBookingId(), this);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(maybeSentenceCalculation))
                .sentenceMonths(monthsOf(maybeSentenceCalculation))
                .sentenceDays(daysOf(maybeSentenceCalculation))
                .releaseDate(safeReleaseDateOf(maybeSentenceCalculation))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(thisOffender.getNomsId())
                .movementFromTo(dischargeMovementFromToOf(offenderMovement))
                .movementCourtCode(dischargeMovementCourtCodeOf(offenderMovement))
                .movementDelete("N")
                .movementCode(dischargeMovementCodeOf(offenderMovement.getMovementReasonCode()))
                .forename1(thisOffender.getFirstName())
                .forename2(thisOffender.getMiddleNames())
                .familyName(thisOffender.getSurname())
                .dateOfBirth(thisOffender.getDateOfBirth().toString())
                .establishmentCode(establishmentCodeOf(offenderMovement, rootOffender))
                .eventType("OffenderDischarge")
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, maybeSentenceCalculation))
                .correlationId(correlationService.nextCorrelationId())
                .build());
    }

    public ExternalMovement asExternalMovement(String jsonStr) {
        if (Strings.isNullOrEmpty(jsonStr)) {
            return null;
        }

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

    public List<ExternalMovement> asPagedExternalMovements(String s) {
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

    public String establishmentCodeOf(ExternalMovement offenderMovement, Offender offender) {
        return mappingService.targetValueOf(
                Optional.ofNullable(offenderMovement)
                        .filter(om -> "OUT".equals(om.getMovementDirection()))
                        .map(x -> x.getFromAgencyLocation().getAgencyLocationId())
                        .orElse(activeBookingOf(offender)
                                .map(booking -> booking.getAgencyLocation().getAgencyLocationId())
                                .orElse(fromAgencyCodeOfLastMovementOutOf(offender))),
                AGENCY_LOCATION_CODE_TYPE);

    }

    private String fromAgencyCodeOfLastMovementOutOf(Offender offender) {
        return Optional.ofNullable(offender.getBookings())
                .flatMap(bookings -> bookings
                .stream()
                .filter(b -> b.getBookingSequence() == 1)
                .findFirst()
                .map(Booking::getLastMovement)
                .filter(m -> "OUT".equals(m.getMovementDirection()))
                .map(m -> m.getFromAgencyLocation().getAgencyLocationId()))
                .orElse(null);
    }

    private Optional<Booking> activeBookingOf(Offender offender) {
        return Optional.ofNullable(offender.getBookings()).flatMap(bookings -> bookings.stream()
                .filter(Booking::getActiveFlag)
                .findFirst());
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

    public List<Sentence> asSentences(String jsonStr) {
        if (Strings.isNullOrEmpty(jsonStr)) {
            return null;
        }

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
        final Offender rootOffender;
        final InmateDetail inmateDetail;
        // OFF_UPD_OASYS event contains either:
        // root offender id only,
        // root offender id and offender id,
        // or booking id and offender id
        if (event.getRootOffenderId() != null) {
            rootOffender = nomisApiServices.getOffenderByOffenderId(event.getRootOffenderId());
            inmateDetail = nomisApiServices.getInmateDetail(bookingIdOf(rootOffender.getBookings()), this);
        } else {
            // event must have a booking id
            inmateDetail = nomisApiServices.getInmateDetail(event.getBookingId(), this);
            rootOffender = nomisApiServices.getOffenderByNomsId(inmateDetail.getOffenderNo());
        }
        final Offender thisOffender = thisOffenderFromRootOffender(rootOffender);
        log.info("... which is for nomsId {}", thisOffender.getNomsId());


        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .oldPrisonNumber(event.getPreviousBookingNumber())
                .nomisId(thisOffender.getNomsId())
                .forename1(thisOffender.getFirstName())
                .forename2(thisOffender.getMiddleNames())
                .familyName(thisOffender.getSurname())
                .eventType("OffenderDetails")
                .establishmentCode(establishmentCodeOf(null, rootOffender))
                .dateOfBirth(thisOffender.getDateOfBirth().toString())
                .correlationId(correlationService.nextCorrelationId())
                .build());
    }

    private Long bookingIdOf(List<Booking> bookings) {
        return bookings.stream().findFirst().map(Booking::getBookingId).orElse(null);
    }

    public Optional<EventMessage> offenderSentenceUpdatedXtagOf(OffenderEvent event) throws ExecutionException, NomisAPIServiceError, RetryException {
        // OFF_SENT_OASYS always has a booking id
        log.info("Handling offenderSentenceUpdated event {}", event);
        final InmateDetail inmateDetail = nomisApiServices.getInmateDetail(event, this);
        final Offender rootOffender = nomisApiServices.getOffenderByNomsId(inmateDetail.getOffenderNo());
        final Offender thisOffender = thisOffenderFromRootOffender(rootOffender);
        log.info("... which is for nomsId {}", thisOffender.getNomsId());

        final List<Sentence> activeSentences = nomisApiServices.getActiveSentences(rootOffender, event.getBookingId(), this);
        final Optional<SentenceCalculation> maybeSentenceCalculation = nomisApiServices.getSentenceCalculation(rootOffender, event.getBookingId(), this);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(maybeSentenceCalculation))
                .sentenceMonths(monthsOf(maybeSentenceCalculation))
                .sentenceDays(daysOf(maybeSentenceCalculation))
                .sentenceDate(sentenceStartDateOf(activeSentences))
                .releaseDate(safeReleaseDateOf(maybeSentenceCalculation))
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(rootOffender))
                .nomisId(thisOffender.getNomsId())
                .forename1(thisOffender.getFirstName())
                .forename2(thisOffender.getMiddleNames())
                .familyName(thisOffender.getSurname())
                .eventType("OffenderSentence")
                .establishmentCode(establishmentCodeOf(null, rootOffender))
                .effectiveSentenceLength(effectiveSentenceLengthOf(activeSentences, maybeSentenceCalculation))
                .dateOfBirth(thisOffender.getDateOfBirth().toString())
                .correlationId(correlationService.nextCorrelationId())
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