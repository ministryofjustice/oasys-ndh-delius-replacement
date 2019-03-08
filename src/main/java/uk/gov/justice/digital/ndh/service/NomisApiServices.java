package uk.gov.justice.digital.ndh.service;

import com.github.rholder.retry.RetryException;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.nomis.ExternalMovement;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.nomis.OffenderImprisonmentStatus;
import uk.gov.justice.digital.ndh.api.nomis.Sentence;
import uk.gov.justice.digital.ndh.api.nomis.SentenceCalculation;
import uk.gov.justice.digital.ndh.api.nomis.elite2.InmateDetail;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class NomisApiServices {

    private final NomisClient custodyApiClient;
    private final NomisClient elite2ApiClient;
    private final OffenderTransformer offenderTransformer;

    @Autowired
    public NomisApiServices(NomisClient custodyApiClient, NomisClient elite2ApiClient, OffenderTransformer offenderTransformer){
        this.custodyApiClient = custodyApiClient;
        this.elite2ApiClient = elite2ApiClient;
        this.offenderTransformer = offenderTransformer;
    }

    public InmateDetail getInmateDetail(OffenderEvent event, XtagTransformer xtagTransformer) throws ExecutionException, NomisAPIServiceError, RetryException {
        return getInmateDetail(event.getBookingId(), xtagTransformer);
    }

    public InmateDetail getInmateDetail(Long bookingId, XtagTransformer xtagTransformer) throws ExecutionException, NomisAPIServiceError, RetryException {
        return elite2ApiClient
                .doGetWithRetry("bookings/" + bookingId, ImmutableMap.of("basicInfo", "true"))
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(xtagTransformer::asInmateDetail).orElseThrow(() -> new NomisAPIServiceError("Can't get inmate detail."));
    }

    ExternalMovement getAdmissionMovement(OffenderEvent event, XtagTransformer xtagTransformer) throws ExecutionException, RetryException, NomisAPIServiceError {
        if (event.getMovementSeq() != null) {
            return getExternalMovement(event, xtagTransformer);
        } else {
            log.warn("Admission movement event {} does not have a movement sequence. Doing fallback by moveent type code ADM.", event);
            return xtagTransformer.getAdmissionMovementFallback(event.getBookingId());
        }
    }

    ExternalMovement getExternalMovement(OffenderEvent event, XtagTransformer xtagTransformer) throws NomisAPIServiceError, ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("movements/bookingId/" + event.getBookingId() + "/sequence/" + event.getMovementSeq())
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(xtagTransformer::asExternalMovement)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender movement for bookingId " + event.getBookingId() + " and sequence " + event.getMovementSeq() + "."));
    }

    ExternalMovement getDischargeMovement(OffenderEvent event, XtagTransformer xtagTransformer) throws ExecutionException, RetryException, NomisAPIServiceError {

        if (event.getMovementSeq() != null) {
            return getExternalMovement(event, xtagTransformer);
        } else {
            log.warn("Discharge movement event {} does not have a movement sequence. Doing fallback by code type TRN", event);
            return getDischargeMovementFallback(event.getBookingId(), xtagTransformer);
        }
    }

    ExternalMovement getDischargeMovementFallback(Long bookingId, XtagTransformer xtagTransformer) throws ExecutionException, RetryException, NomisAPIServiceError {
        return getExternalMovements(bookingId, xtagTransformer)
                .flatMap(ems -> ems.stream()
                        .filter(em -> "TRN".equals(em.getMovementTypeCode()))
                        .findFirst())
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender TRN movement for bookingId " + bookingId));
    }

    List<Sentence> getActiveSentences(Offender offender, Long bookingId, XtagTransformer xtagTransformer) throws ExecutionException, NomisAPIServiceError, RetryException {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentences", ImmutableMap.of("bookingId", bookingId))
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(xtagTransformer::asSentences)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender sentence calculations."));
    }

    List<OffenderImprisonmentStatus> getImprisonmentStatuses(Offender rootOffender, OffenderEvent event, XtagTransformer xtagTransformer) throws ExecutionException, RetryException, NomisAPIServiceError {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + rootOffender.getOffenderId() + "/imprisonmentStatuses")
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(xtagTransformer::asImprisonmentStatuses)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender imprisonment statuses."));
    }

    public Offender getOffenderByNomsId(String nomsId) throws NomisAPIServiceError, ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("offenders/nomsId/" + nomsId)
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(offenderTransformer::asOffender).orElseThrow(() -> new NomisAPIServiceError("Can't get offender detail."));
    }

    public Optional<SentenceCalculation> getSentenceCalculation(Offender offender, Long bookingId, XtagTransformer xtagTransformer) throws ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentenceCalculations", ImmutableMap.of("bookingId", bookingId))
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(xtagTransformer::asSentenceCalculations)
                .flatMap(sentences -> sentences.stream().findFirst());
    }

    public Optional<List<ExternalMovement>> getExternalMovements(Long bookingId, XtagTransformer xtagTransformer) throws ExecutionException, RetryException {
        return custodyApiClient
                .doGetWithRetry("/movements", ImmutableMap.of("bookingId", bookingId, "size", 2000))
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(xtagTransformer::asPagedExternalMovements);
    }

    public Offender getOffenderByOffenderId(Long offenderId) throws ExecutionException, RetryException, NomisAPIServiceError {
        return custodyApiClient
                .doGetWithRetry("offenders/offenderId/" + offenderId)
                .filter(this::okOrNotFound)
                .map(HttpResponse::getBody)
                .map(offenderTransformer::asOffender)
                .orElseThrow(() -> new NomisAPIServiceError("Can't get offender " + offenderId));

    }

    public boolean okOrNotFound(HttpResponse<String> r) {
        return r.getStatus() == HttpStatus.OK.value() || r.getStatus() == HttpStatus.NOT_FOUND.value();
    }
}