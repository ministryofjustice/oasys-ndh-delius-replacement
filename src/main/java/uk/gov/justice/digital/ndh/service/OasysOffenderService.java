package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.nomis.Booking;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.oasys.request.OffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetail;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final NomisClient nomisClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public OasysOffenderService(OffenderTransformer offenderTransformer,
                                CommonTransformer commonTransformer,
                                FaultTransformer faultTransformer, ExceptionLogService exceptionLogService,
                                MessageStoreService messageStoreService,
                                @Qualifier("initialSearchClient") DeliusSOAPClient deliusInitialSearchClient,
                                @Qualifier("offenderDetailsClient") DeliusSOAPClient deliusOffenderDetailsClient,
                                NomisClient nomisClient,
                                XmlMapper xmlMapper,
                                @Qualifier("globalObjectMapper") ObjectMapper objectMapper) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper, faultTransformer);
        this.offenderTransformer = offenderTransformer;
        this.deliusInitialSearchClient = deliusInitialSearchClient;
        this.faultTransformer = faultTransformer;
        this.deliusOffenderDetailsClient = deliusOffenderDetailsClient;
        this.nomisClient = nomisClient;
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


    private Optional<SoapEnvelope> deliusInitialSearchResponseOf(Optional<String> maybeRawResponse, String correlationId) {
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

    public Optional<String> offenderDetails(String offenderDetailsRequestXml) {
        val maybeOasysOffenderDetailsRequest = commonTransformer.asSoapEnvelope(offenderDetailsRequestXml);

        val correlationId = maybeOasysOffenderDetailsRequest.map(offenderDetails -> offenderDetails.getBody().getOffenderDetailsRequest().getHeader().getCorrelationID()).orElse(null);
        val offenderId = maybeOasysOffenderDetailsRequest.map(offenderDetails -> offenderIdOf(offenderDetails.getBody().getOffenderDetailsRequest())).orElse(null);

        logMessage(correlationId, offenderDetailsRequestXml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);

        final Optional<String> stringResponse = maybeOasysOffenderDetailsRequest.flatMap(
                rq -> Optional.ofNullable(rq.getBody().getOffenderDetailsRequest().getNomisId()))
                .map(nomisId -> deliusOffenderDetailsOf(maybeOasysOffenderDetailsRequest, correlationId, offenderId))
                .orElse(nomisOffenderDetailsOf(maybeOasysOffenderDetailsRequest, correlationId, offenderId));


        return stringResponse;
    }

    private String offenderIdOf(OffenderDetailsRequest offenderDetailsRequest) {
        return Optional.ofNullable(offenderDetailsRequest.getNomisId())
                .orElse(Optional.ofNullable(offenderDetailsRequest.getCmsProbNumber()).orElse(null));
    }

    private Optional<String> nomisOffenderDetailsOf(Optional<SoapEnvelope> maybeOasysOffenderDetailsRequest, String correlationId, String offenderId) {

        final Optional<Offender> maybeOffender = maybeOasysOffenderDetailsRequest.flatMap(
                rq -> {
                    try {
                        return nomisClient.doGetWithRetry("offenders/offenderId/" + offenderId)
                                .map(HttpResponse::getBody)
                                .map(this::asOffender);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
        );

        final Booking latestBooking = maybeOffender.flatMap(offender -> offender.getBookings().stream().findFirst()).orElse(null);

        final Optional<SoapEnvelope> oasysOffenderDetailsResponse = maybeOffender.map(offender -> SoapEnvelope
                .builder()
                .header(SoapHeader.builder().build())
                .body(SoapBody
                        .builder()
                        .offenderDetailsResponse(OffenderDetailsResponse
                                .builder()
                                .offenderDetail(
                                        OffenderDetail
                                                .builder()
                                                .prisonNumber(latestBooking.getBookingNo())
                                                .nomisId(offender.getNomsId())
                                                .familyName(offender.getSurname())
                                                .forename1(offender.getFirstName())
                                                .forename2(forename2Of(offender.getMiddleNames()))
                                                .forename3(forename3Of(offender.getMiddleNames()))
                                                .gender(offenderTransformer.oasysGenderOf(offender.getGender().getCode()))
                                                .dateOfBirth(XMLFormattedDateOf(offender.getDateOfBirth()))
                                                .pnc(pncOf(offender.getIdentifiers()))
                                                //TODO: No idea which date.
//                                                .releaseDate(?)


                                                .build()
                                )
                                .build())
                        .build())
                .build());
        return null;


    }

    private String XMLFormattedDateOf(LocalDate date) {
        return Optional.ofNullable(date).map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).orElse(null);
    }

    private String pncOf(List<Identifier> identifiers) {
        return Optional.ofNullable(identifiers).flatMap(ids -> ids
                .stream()
                .filter(identifier -> "PNC".equals(identifier.getIdentifierType()))
                .findFirst()
                .map(Identifier::getIdentifier)).orElse(null);
    }

    private String forename2Of(String middleNames) {
        return Optional.ofNullable(middleNames).map(mns -> mns.split(" ")[0]).orElse(null);
    }

    private String forename3Of(String middleNames) {
        return Optional.ofNullable(forename2Of(middleNames)).map(fn2 -> middleNames.replaceFirst(fn2, "").trim()).filter(s -> !s.isEmpty()).orElse(null);
    }

    private Offender asOffender(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, Offender.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return Offender.builder().build();
        }
    }

    public Optional<String> deliusOffenderDetailsOf(Optional<SoapEnvelope> maybeOasysOffenderDetailsRequest, String correlationId, String offenderId) {
        val maybeTransformed = maybeOasysOffenderDetailsRequest.map(offenderTransformer::deliusOffenderDetailsRequestOf);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId);

        maybeTransformedXml.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation));

        val maybeRawResponse = rawDeliusOffenderDetailsResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusOffenderDetailsResponseOf(maybeRawResponse, correlationId);

        return handleResponse(maybeOasysOffenderDetailsRequest, correlationId, offenderId, maybeRawResponse, maybeResponse, offenderTransformer.offenderDetailsResponseTransform);
    }

    public Optional<String> handleResponse(Optional<SoapEnvelope> maybeOasysOffenderDetailsRequest, String correlationId, String offenderId, Optional<String> maybeRawResponse, Optional<SoapEnvelope> maybeResponse, BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> transform) {
        if (maybeResponse.isPresent()) {
            if (maybeResponse.get().getBody().isSoapFault()) {
                return handleSoapFault(correlationId, maybeRawResponse, maybeResponse.get().toString());
            }
        }

        maybeRawResponse.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_OutboundBeforeTransformation));

        Optional<SoapEnvelope> maybeOasysSOAPResponse;
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

    private Optional<SoapEnvelope> deliusOffenderDetailsResponseOf(Optional<String> maybeRawResponse, String correlationId) {
        return getSoapEnvelope(maybeRawResponse, correlationId, "Can't deserialize delius offender details response: ");
    }

    private Optional<SoapEnvelope> getSoapEnvelope(Optional<String> maybeRawResponse, String correlationId, String descriptionPreamble) {
        return maybeRawResponse.flatMap(rawResponse -> {
            try {
                return Optional.of(xmlMapper.readValue(rawResponse, SoapEnvelope.class));
            } catch (IOException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(rawResponse, correlationId, descriptionPreamble + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<String> rawDeliusOffenderDetailsResponseOf(Optional<String> maybeTransformedXml, String correlationId) {
        return maybeTransformedXml.flatMap((String transformedXml) -> callDeliusOffenderDetails(transformedXml, correlationId, deliusOffenderDetailsClient));
    }

    private Optional<String> callDeliusOffenderDetails(String transformedXml, String correlationId, DeliusSOAPClient soapClient) {
        return doCall(transformedXml, correlationId, soapClient, "Can't talk to Delius offender details endpoint: ");
    }

    private Optional<String> doCall(String transformedXml, String correlationId, DeliusSOAPClient deliusSOAPClient, String descriptionPreamble) {
        try {
            return Optional.of(deliusSOAPClient.deliusWebServiceResponseOf(transformedXml));
        } catch (UnirestException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(transformedXml, correlationId, descriptionPreamble + e.getMessage());
            return Optional.empty();
        }
    }

}
