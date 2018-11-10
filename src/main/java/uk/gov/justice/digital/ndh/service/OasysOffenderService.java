package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.nomis.Address;
import uk.gov.justice.digital.ndh.api.nomis.AgencyLocation;
import uk.gov.justice.digital.ndh.api.nomis.Alert;
import uk.gov.justice.digital.ndh.api.nomis.Booking;
import uk.gov.justice.digital.ndh.api.nomis.CourtEvent;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderAssessment;
import uk.gov.justice.digital.ndh.api.nomis.OffenderImprisonmentStatus;
import uk.gov.justice.digital.ndh.api.nomis.Physicals;
import uk.gov.justice.digital.ndh.api.nomis.Sentence;
import uk.gov.justice.digital.ndh.api.nomis.SentenceCalculation;
import uk.gov.justice.digital.ndh.api.oasys.request.OffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
@Slf4j
public class OasysOffenderService extends RequestResponseService {
    private static final String NDH_WEB_SERVICE_SEARCH = "NDH_Web_Service_Search";
    private final OffenderTransformer offenderTransformer;
    private final FaultTransformer faultTransformer;
    private final DeliusSOAPClient deliusInitialSearchClient;
    private final DeliusSOAPClient deliusOffenderDetailsClient;
    private final NomisClient custodyApiClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public OasysOffenderService(OffenderTransformer offenderTransformer,
                                CommonTransformer commonTransformer,
                                FaultTransformer faultTransformer, ExceptionLogService exceptionLogService,
                                MessageStoreService messageStoreService,
                                @Qualifier("initialSearchClient") DeliusSOAPClient deliusInitialSearchClient,
                                @Qualifier("offenderDetailsClient") DeliusSOAPClient deliusOffenderDetailsClient,
                                @Qualifier("custodyApiClient") NomisClient custodyApiClient,
                                XmlMapper xmlMapper,
                                @Qualifier("globalObjectMapper")
                                        ObjectMapper objectMapper) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper, faultTransformer);
        this.offenderTransformer = offenderTransformer;
        this.deliusInitialSearchClient = deliusInitialSearchClient;
        this.faultTransformer = faultTransformer;
        this.deliusOffenderDetailsClient = deliusOffenderDetailsClient;
        this.custodyApiClient = custodyApiClient;
        this.objectMapper = objectMapper;
    }

    public Optional<String> initialSearch(String initialSearchXml) {
        val maybeOasysInitialSearch = commonTransformer.asSoapEnvelope(initialSearchXml);

        val correlationId = maybeOasysInitialSearch.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getHeader().getCorrelationID()).orElse(null);
        val offenderId = maybeOasysInitialSearch.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getCmsProbNumber()).orElse(null);

        logMessage(correlationId, initialSearchXml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);

        val maybeTransformed = maybeOasysInitialSearch.map(offenderTransformer::deliusInitialSearchRequestOf);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId);

        maybeTransformedXml.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation));

        val maybeRawResponse = rawDeliusInitialSearchResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusInitialSearchResponseOf(maybeRawResponse, correlationId);

        return handleResponse(maybeOasysInitialSearch, correlationId, offenderId, maybeRawResponse, maybeResponse, offenderTransformer.initialSearchResponseTransform);
    }


    private Optional<SoapEnvelopeSpec1_2> deliusInitialSearchResponseOf(Optional<String> maybeRawResponse, String correlationId) {
        return getSoapEnvelope(maybeRawResponse, correlationId, "Can't deserialize delius initial search response: ");
    }

    private Optional<String> rawDeliusInitialSearchResponseOf(Optional<String> maybeTransformedXml, String correlationId) {
        return maybeTransformedXml.flatMap((String transformedXml) -> callDeliusInitialSearch(transformedXml, correlationId));
    }

    private void logMessage(String correlationId, String xml, String offenderId, MessageStoreService.ProcStates procState) {
        messageStoreService.writeMessage(xml, correlationId, offenderId, NDH_WEB_SERVICE_SEARCH, procState);
    }

    private Optional<String> callDeliusInitialSearch(String transformedXml, String correlationId) {
        return doCall(transformedXml, correlationId, deliusInitialSearchClient, "Can't talk to Delius initial search endpoint: ");
    }

    public Optional<String> offenderDetails(String offenderDetailsRequestXml) throws JsonProcessingException {
        val maybeOasysOffenderDetailsRequest = commonTransformer.asSoapEnvelope(offenderDetailsRequestXml);

        val correlationId = maybeOasysOffenderDetailsRequest.map(offenderDetails -> offenderDetails.getBody().getOffenderDetailsRequest().getHeader().getCorrelationID()).orElse(null);
        val offenderId = maybeOasysOffenderDetailsRequest.map(offenderDetails -> offenderIdOf(offenderDetails.getBody().getOffenderDetailsRequest())).orElse(null);

        logMessage(correlationId, offenderDetailsRequestXml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);

        final Optional<String> maybeProbationId = maybeOasysOffenderDetailsRequest.flatMap(
                rq -> Optional.ofNullable(rq.getBody().getOffenderDetailsRequest().getCmsProbNumber()));

        final Optional<String> maybeCustodyId = maybeOasysOffenderDetailsRequest.flatMap(
                rq -> Optional.ofNullable(rq.getBody().getOffenderDetailsRequest().getNomisId()));

        final Optional<String> maybeProbationDetails = maybeProbationId
                .flatMap(probId -> deliusOffenderDetailsOf(maybeOasysOffenderDetailsRequest, correlationId, probId));

        final Optional<String> maybeCustodyDetails = maybeCustodyId
                .flatMap(custId -> {
                    try {
                        return nomisOffenderDetailsOf(maybeOasysOffenderDetailsRequest, correlationId, custId);
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                });

        return Optional.ofNullable(maybeProbationDetails.orElse(maybeCustodyDetails.orElse(null)));
    }

    private String offenderIdOf(OffenderDetailsRequest offenderDetailsRequest) {
        return Optional.ofNullable(offenderDetailsRequest.getNomisId())
                .orElse(Optional.ofNullable(offenderDetailsRequest.getCmsProbNumber()).orElse(null));
    }

    private Optional<String> nomisOffenderDetailsOf(Optional<SoapEnvelopeSpec1_2> maybeOasysOffenderDetailsRequest, String correlationId, String nomsId) throws JsonProcessingException {

        final Optional<Offender> maybeOffender = maybeOffenderOf(maybeOasysOffenderDetailsRequest, nomsId);

        final Booking latestBooking = bookingOf(maybeOffender).orElse(null);

        final Optional<SentenceCalculation> maybeSentenceCalc = maybeSentenceCalculationOf(maybeOffender, latestBooking);

        final Optional<Sentence> maybeSentence = maybeSentenceOf(maybeOffender, latestBooking);

        final Optional<OffenderImprisonmentStatus> maybeImprisonmentStatus = maybeImprisonmentStatusOf(maybeOffender, latestBooking);

        final Optional<List<CourtEvent>> maybeCourtEvents = maybeCourtEventsOf(maybeOffender, latestBooking);

        final Optional<AgencyLocation> sentencingCourtAgencyLocation = sentencingAgencyLocationOf(maybeCourtEvents);

        final Optional<List<Address>> maybeAddresses = maybeAddressesOf(maybeOffender, latestBooking);

        final Optional<Address> maybeHomeAddress = maybeHomeAddressOf(maybeAddresses);

        final Optional<Address> maybeDischargeAddress = maybeDischargeAddressOf(maybeAddresses);

        final Optional<List<Physicals>> maybePhysicals = maybePhysicalsOf(maybeOffender, latestBooking);

        final Optional<List<OffenderAssessment>> maybeAssessments = maybeAssessmentsOf(maybeOffender, latestBooking);

        final Optional<List<Alert>> maybeAlerts = maybeF2052AlertsOf(maybeOffender, latestBooking);

        final Optional<SoapEnvelopeSpec1_2> oasysOffenderDetailsResponse = maybeOffender.map(offender -> offenderTransformer.oasysOffenderDetailResponseOf(maybeOasysOffenderDetailsRequest, maybeOffender, latestBooking, maybeSentenceCalc, maybeSentence, maybeImprisonmentStatus, maybeCourtEvents, sentencingCourtAgencyLocation, maybeHomeAddress, maybeDischargeAddress, maybePhysicals, maybeAssessments, maybeAlerts, offender));

        Optional<String> response;

        if (oasysOffenderDetailsResponse.isPresent()) {
            response = Optional.of(commonTransformer.asString(oasysOffenderDetailsResponse.get()));
        } else {
            response = Optional.of(notFound(correlationId, nomsId));
        }

        response.ifPresent(s -> logMessage(correlationId, s, nomsId, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation));

        return response;
    }

    private Optional<List<Alert>> maybeF2052AlertsOf(Optional<Offender> maybeOffender, Booking latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/alerts",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId(),
                                                "alertType", "H",
                                                "alertCode", "HA",
                                                "alertStatus", "ACTIVE"))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asAlerts);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                });
    }

    private List<Alert> asAlerts(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, Alert[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }


    private Optional<List<OffenderAssessment>> maybeAssessmentsOf(Optional<Offender> maybeOffender, Booking latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/assessments",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asOffenderAssessments);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );

    }

    private List<OffenderAssessment> asOffenderAssessments(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, OffenderAssessment[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private String notFound(String correlationId, String nomsId) {
        return faultTransformer.businessSoapFaultOf(notFoundInNOMISMessage(nomsId), correlationId, notFoundInNOMISMessage(nomsId));
    }

    private String notFoundInNOMISMessage(String nomsId) {
        return "No offender details returned from NOMIS for offender - " + nomsId;
    }

    private Optional<List<Physicals>> maybePhysicalsOf(Optional<Offender> maybeOffender, Booking latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/physicals",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asPhysicals);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private List<Physicals> asPhysicals(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, Physicals[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private Optional<Address> maybeHomeAddressOf(Optional<List<Address>> maybeAddresses) {
        return maybeAddressOf(maybeAddresses, ImmutableList.of("HOME"));
    }

    private Optional<Address> maybeDischargeAddressOf(Optional<List<Address>> maybeAddresses) {
        return maybeAddressOf(maybeAddresses, ImmutableList.of("RELEASE", "DBH", "DNF", "DUT", "DST", "DPH", "DAP", "DBA", "DOH", "DSH"));
    }

    private Optional<Address> maybeAddressOf
            (Optional<List<Address>> maybeAddresses, List<String> addressTypes) {
        return maybeAddresses.flatMap(addresses -> addresses
                .stream()
                .filter(address -> !(Optional.ofNullable(address.getAddressUsages()).orElse(Collections.emptyList()).isEmpty()))
                .filter(address -> address.getAddressUsages().stream().anyMatch(usage -> usage.getActive() && addressTypes.contains(usage.getUsage().getCode())))
                .findFirst());
    }

    private Optional<List<Address>> maybeAddressesOf(Optional<Offender> maybeOffender, Booking latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/addresses",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asAddresses);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private List<Address> asAddresses(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, Address[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private Optional<List<CourtEvent>> maybeCourtEventsOf(Optional<Offender> maybeOffender, Booking
            latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/courtEvents",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asCourtEvents);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private List<CourtEvent> asCourtEvents(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, CourtEvent[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private Optional<OffenderImprisonmentStatus> maybeImprisonmentStatusOf
            (Optional<Offender> maybeOffender, Booking latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/imprisonmentStatuses",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asImprisonmentStatuses)
                                .flatMap(statuses -> statuses.stream()
                                        .filter(OffenderImprisonmentStatus::getLatestStatus)
                                        .findFirst());

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private Optional<Sentence> maybeSentenceOf(Optional<Offender> maybeOffender, Booking latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentences",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asSentences)
                                .flatMap(sentences -> sentences.stream()
                                        .filter(Sentence::getIsActive)
                                        .min(Comparator.comparing(Sentence::getSentenceSequenceNumber)));

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private boolean filter(Sentence s) {
        return s.getIsActive();
    }

    private Optional<SentenceCalculation> maybeSentenceCalculationOf(Optional<Offender> maybeOffender, Booking
            latestBooking) {
        return maybeOffender.flatMap(
                offender -> {
                    try {
                        return custodyApiClient
                                .doGetWithRetry("offenders/offenderId/" + offender.getOffenderId() + "/sentenceCalculations",
                                        ImmutableMap.of("bookingId", latestBooking.getBookingId()))
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(this::asSentenceCalculations)
                                .flatMap(sentenceCalculations -> sentenceCalculations.stream().findFirst());

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private Optional<Booking> bookingOf(Optional<Offender> maybeOffender) {
        return maybeOffender.flatMap(offender -> offender.getBookings().stream().findFirst());
    }

    private Optional<Offender> maybeOffenderOf(Optional<SoapEnvelopeSpec1_2> maybeOasysOffenderDetailsRequest, String nomsId) {
        return maybeOasysOffenderDetailsRequest.flatMap(
                rq -> {
                    try {
                        return custodyApiClient.doGetWithRetry("offenders/nomsId/" + nomsId)
                                .filter(r -> r.getStatus() == HttpStatus.OK.value())
                                .map(HttpResponse::getBody)
                                .map(offenderTransformer::asOffender);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );
    }

    private Optional<AgencyLocation> sentencingAgencyLocationOf(Optional<List<CourtEvent>> maybeCourtEvents) {
        return maybeCourtEvents
                .flatMap(ces -> ces
                        .stream()
                        .filter(ce -> "SENTENCE".equals(ce.getComments()))
                        .findFirst())
                .flatMap(ce -> Optional.of(ce.getAgencyLocation()));
    }


    private List<OffenderImprisonmentStatus> asImprisonmentStatuses(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, OffenderImprisonmentStatus[].class));
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Sentence> asSentences(String jsonStr) {
        try {
            return Arrays.asList(objectMapper.readValue(jsonStr, Sentence[].class));
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

    public Optional<String> deliusOffenderDetailsOf
            (Optional<SoapEnvelopeSpec1_2> maybeOasysOffenderDetailsRequest, String correlationId, String offenderId) {
        val maybeTransformed = maybeOasysOffenderDetailsRequest.map(offenderTransformer::deliusOffenderDetailsRequestOf);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId);

        maybeTransformedXml.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation));

        val maybeRawResponse = rawDeliusOffenderDetailsResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusOffenderDetailsResponseOf(maybeRawResponse, correlationId);

        return handleResponse(maybeOasysOffenderDetailsRequest, correlationId, offenderId, maybeRawResponse, maybeResponse, offenderTransformer.offenderDetailsResponseTransform);
    }

    public Optional<String> handleResponse(Optional<SoapEnvelopeSpec1_2> maybeOasysOffenderDetailsRequest,
                                           String correlationId,
                                           String offenderId,
                                           Optional<String> maybeRawResponse,
                                           Optional<SoapEnvelopeSpec1_2> maybeResponse,
                                           BiFunction<Optional<SoapEnvelopeSpec1_2>, Optional<SoapEnvelopeSpec1_2>, Optional<SoapEnvelopeSpec1_2>> transform) {
        if (maybeResponse.isPresent()) {
            if (maybeResponse.get().getBody().isSoapFault()) {
                return handleSoapFault(correlationId, maybeRawResponse, maybeResponse.get().toString());
            }
        }

        maybeRawResponse.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_OutboundBeforeTransformation));

        Optional<SoapEnvelopeSpec1_2> maybeOasysSOAPResponse;
        try {
            maybeOasysSOAPResponse = transform.apply(maybeOasysOffenderDetailsRequest, maybeResponse);
        } catch (NDHMappingException ndhme) {
            exceptionLogService.logMappingFail(ndhme.getCode(), ndhme.getValue(), ndhme.getSubject(), correlationId, offenderId);
            return Optional.ofNullable(faultTransformer.mappingSoapFaultOf(ndhme, correlationId));
        }

        Optional<String> maybeXmlResponse = maybeOasysSOAPResponse.map(oasysResponse -> handleOkResponse(correlationId, oasysResponse));

        maybeXmlResponse.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation));

        return maybeXmlResponse;
    }

    private Optional<SoapEnvelopeSpec1_2> deliusOffenderDetailsResponseOf(Optional<String> maybeRawResponse, String
            correlationId) {
        return getSoapEnvelope(maybeRawResponse, correlationId, "Can't deserialize delius offender details response: ");
    }

    private Optional<SoapEnvelopeSpec1_2> getSoapEnvelope(Optional<String> maybeRawResponse, String
            correlationId, String descriptionPreamble) {
        return maybeRawResponse.flatMap(rawResponse -> {
            try {
                return Optional.of(xmlMapper.readValue(rawResponse, SoapEnvelopeSpec1_2.class));
            } catch (IOException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(rawResponse, correlationId, descriptionPreamble + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<String> rawDeliusOffenderDetailsResponseOf(Optional<String> maybeTransformedXml, String
            correlationId) {
        return maybeTransformedXml.flatMap((String transformedXml) -> callDeliusOffenderDetails(transformedXml, correlationId, deliusOffenderDetailsClient));
    }

    private Optional<String> callDeliusOffenderDetails(String transformedXml, String
            correlationId, DeliusSOAPClient soapClient) {
        return doCall(transformedXml, correlationId, soapClient, "Can't talk to Delius offender details endpoint: ");
    }

    private Optional<String> doCall(String transformedXml, String correlationId, DeliusSOAPClient
            deliusSOAPClient, String descriptionPreamble) {
        try {
            return Optional.of(deliusSOAPClient.deliusWebServiceResponseOf(transformedXml));
        } catch (UnirestException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(transformedXml, correlationId, descriptionPreamble + e.getMessage());
            return Optional.empty();
        }
    }
}
