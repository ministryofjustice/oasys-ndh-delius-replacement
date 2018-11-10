package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.nomis.ExternalMovement;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.nomis.OffenderImprisonmentStatus;
import uk.gov.justice.digital.ndh.api.nomis.SentenceCalculation;
import uk.gov.justice.digital.ndh.api.nomis.elite2.InmateDetail;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class XtagTransformer {

    public static final long IMPRISONMENT_STATUS_CODE_TYPE = 14L;
    public static final long AGENCY_LOCATION_CODE_TYPE = 2005L;
    public static final String OFFENDER_LIFER = "OffenderLifer";
    public static final String STATUS_CHANGE = "StatusChange";
    private final NomisClient custodyApiClient;
    private final NomisClient elite2ApiClient;
    private final ObjectMapper objectMapper;
    private final MappingService mappingService;
    private final OffenderTransformer offenderTransformer;


    public XtagTransformer(NomisClient custodyApiClient, NomisClient elite2ApiClient, @Qualifier("globalObjectMapper") ObjectMapper objectMapper, MappingService mappingService, OffenderTransformer offenderTransformer) {
        this.custodyApiClient = custodyApiClient;
        this.elite2ApiClient = elite2ApiClient;
        this.objectMapper = objectMapper;
        this.mappingService = mappingService;
        this.offenderTransformer = offenderTransformer;
    }

    public Optional<EventMessage> offenderImprisonmentStatusUpdatedXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        /*
        source:
                .eventType("IMPRISONMENT_STATUS-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .imprisonmentStatusSeq(longOf(xtag.getContent().getP_imprison_status_seq()))
                .nomisEventType(xtag.getEventType())
         */

        /*
        target:
            timestamp
            prisonnumber
            pnc
            nomisId
            forename1
            forename2
            familyName
            eventTpe (1 or Lifer)
            establishmentCode
            dateOfBirth
            correlationId
         */

        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);

        Optional<OffenderImprisonmentStatus> maybeOffenderImprisonmentStatus = custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/imprisonmentStatuses")
                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                .map(HttpResponse::getBody)
                .map(this::asImprisonmentStatuses)
                .flatMap(imprisonmentStatuses -> imprisonmentStatuses.stream()
                        .filter(imprisonmentStatus -> imprisonmentStatus.getImprisonmentStatus().getImprisonmentStatusSeq().equals(event.getImprisonmentStatusSeq()))
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
                .correlationId(randomUuid())
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
                        .findFirst()).map(Identifier::getIdentifier).orElse(null);
    }

    private InmateDetail asInmateDetail(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, InmateDetail.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public Optional<EventMessage> offenderReceptionXtagOf(OffenderEvent event) {
        return null;
    }


    public Optional<EventMessage> bookingUpdatedXtagOf(OffenderEvent event) {
        return null;
    }

    public Optional<EventMessage> offenderDischargeXtagOf(OffenderEvent event) throws ExecutionException, UnirestException, NomisAPIServiceError {
        /*
        xtag:
        .eventType("OFFENDER_MOVEMENT-DISCHARGE")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
                .nomisEventType(xtag.getEventType())
         */

        /*
        mapped:
        <ns0:TIMESTAMP-VARCHAR2-IN>20181106080651.96358</ns0:TIMESTAMP-VARCHAR2-IN>
   <ns0:SENTENCEYEARS-VARCHAR2-IN>9</ns0:SENTENCEYEARS-VARCHAR2-IN>
   <ns0:SENTENCEMONTHS-VARCHAR2-IN>4</ns0:SENTENCEMONTHS-VARCHAR2-IN>
   <ns0:SENTENCEDAYS-VARCHAR2-IN>0</ns0:SENTENCEDAYS-VARCHAR2-IN>
   <ns0:RELEASEDATE-VARCHAR2-IN>2019-06-27</ns0:RELEASEDATE-VARCHAR2-IN>
   <ns0:PRISONNUMBER-VARCHAR2-IN>N74862</ns0:PRISONNUMBER-VARCHAR2-IN>
   <ns0:PNC-VARCHAR2-IN>11/504826Q</ns0:PNC-VARCHAR2-IN>
   <ns0:NOMISID-VARCHAR2-IN>A6984DH</ns0:NOMISID-VARCHAR2-IN>
   <ns0:MOVEMENTFROMORTO-VARCHAR2-IN />
   <ns0:MOVEMENTDELETE-VARCHAR2-IN>N</ns0:MOVEMENTDELETE-VARCHAR2-IN>
   <ns0:MOVEMENTCODE-VARCHAR2-IN>R2</ns0:MOVEMENTCODE-VARCHAR2-IN>
   <ns0:FORENAME2-VARCHAR2-IN>TESTER</ns0:FORENAME2-VARCHAR2-IN>
   <ns0:FORENAME1-VARCHAR2-IN>TESTING</ns0:FORENAME1-VARCHAR2-IN>
   <ns0:FAMILYNAME-VARCHAR2-IN>TESTS</ns0:FAMILYNAME-VARCHAR2-IN>
   <ns0:EVENT_TYPE-VARCHAR2-IN>OffenderDischarge</ns0:EVENT_TYPE-VARCHAR2-IN>
   <ns0:ESTABLISHMENTCODE-VARCHAR2-IN>580</ns0:ESTABLISHMENTCODE-VARCHAR2-IN>
   <ns0:EFFECTIVESENTENCELENGTH-VARCHAR2-IN>3410</ns0:EFFECTIVESENTENCELENGTH-VARCHAR2-IN>
   <ns0:DATEOFBIRTH-VARCHAR2-IN>1979-02-18</ns0:DATEOFBIRTH-VARCHAR2-IN>
   <ns0:CORRELATIONID-VARCHAR2-IN>NOMISHNOMIS20181106080653313013</ns0:CORRELATIONID-VARCHAR2-IN>
         */

        final InmateDetail inmateDetail = getInmateDetail(event);
        final Offender offender = getOffender(inmateDetail);
        final SentenceCalculation sentence = getSentenceCalculation(offender, inmateDetail);
        final ExternalMovement offenderMovement = getMovement(offender, inmateDetail, event);

        return Optional.ofNullable(EventMessage.builder()
                .timestamp(oasysTimestampOf(event.getEventDatetime()))
                .sentenceYears(yearsOf(sentence.getEffectiveSentenceLength()))
                .sentenceMonths(monthsOf(sentence.getEffectiveSentenceLength()))
                .sentenceDays(monthsOf(sentence.getEffectiveSentenceLength()))
                .releaseDate(sentence.getReleaseDate().toString())
                .prisonNumber(inmateDetail.getBookingNo())
                .pnc(pncOf(offender))
                .nomisId(offender.getNomsId())
                .movementFromTo("")
                .movementDelete("N")
                .movementCode(offenderMovement.getMovementReasonCode())
                .forename1(offender.getFirstName())
                .forename2(offender.getMiddleNames())
                .familyName(offender.getSurname())
                .dateOfBirth(offender.getDateOfBirth().toString())
                .correlationId(randomUuid())
                .build());

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

    public String randomUuid() {
        return UUID.randomUUID().toString();
    }

    public Optional<EventMessage> offenderUpdatedXtagOf(OffenderEvent event) {
        return null;
    }

    public Optional<EventMessage> offenderSentenceUpdatedXtagOf(OffenderEvent event) {
        return null;
    }
}