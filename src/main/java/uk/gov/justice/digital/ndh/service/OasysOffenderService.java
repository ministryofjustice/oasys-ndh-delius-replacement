package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
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

    @Autowired
    public OasysOffenderService(OffenderTransformer offenderTransformer,
                                CommonTransformer commonTransformer,
                                ExceptionLogService exceptionLogService,
                                MessageStoreService messageStoreService,
                                @Qualifier("initialSearchClient") DeliusSOAPClient deliusInitialSearchClient,
                                @Qualifier("offenderDetailsClient") DeliusSOAPClient deliusOffenderDetailsClient,
                                XmlMapper xmlMapper,
                                FaultTransformer faultTransformer) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper, faultTransformer);
        this.offenderTransformer = offenderTransformer;
        this.deliusInitialSearchClient = deliusInitialSearchClient;
        this.faultTransformer = faultTransformer;
        this.deliusOffenderDetailsClient = deliusOffenderDetailsClient;
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

        val correlationId = maybeOasysOffenderDetailsRequest.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getHeader().getCorrelationID()).orElse(null);
        val offenderId = maybeOasysOffenderDetailsRequest.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getCmsProbNumber()).orElse(null);

        logMessage(correlationId, offenderDetailsRequestXml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);

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
            exceptionLogService.logMappingFail(ndhme.getCode(), ndhme.getSourceValue(), ndhme.getSubject(), correlationId, offenderId);
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
