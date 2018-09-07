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

@Service
@Slf4j
public class OasysOffenderService extends RequestResponseService {
    private static final String NDH_WEB_SERVICE_SEARCH = "NDH_Web_Service_Search";
    private final OffenderTransformer offenderTransformer;
    private final FaultTransformer faultTransformer;
    private final DeliusSOAPClient deliusInitialSearchClient;

    @Autowired
    public OasysOffenderService(OffenderTransformer offenderTransformer,
                                CommonTransformer commonTransformer,
                                ExceptionLogService exceptionLogService,
                                MessageStoreService messageStoreService,
                                @Qualifier("initialSearchClient") DeliusSOAPClient deliusInitialSearchClient,
                                XmlMapper xmlMapper,
                                FaultTransformer faultTransformer) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper, faultTransformer);
        this.offenderTransformer = offenderTransformer;
        this.deliusInitialSearchClient = deliusInitialSearchClient;
        this.faultTransformer = faultTransformer;
    }

    public Optional<String> initialSearch(String initialSearchXml) {
        val maybeOasysInitialSearch = commonTransformer.asSoapEnvelope(initialSearchXml);

        val correlationId = maybeOasysInitialSearch.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getHeader().getCorrelationID()).orElse(null);
        val offenderId = maybeOasysInitialSearch.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getCmsProbNumber()).orElse(null);

        logMessage(correlationId, initialSearchXml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);

        val maybeTransformed = deliusInitialSearchRequestOf(maybeOasysInitialSearch);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId);

        maybeTransformedXml.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_InboundAfterTransformation));

        val maybeRawResponse = rawDeliusInitialSearchResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusInitialSearchResponseOf(maybeRawResponse, correlationId);

        if (maybeResponse.isPresent()) {
            if (maybeResponse.get().getBody().isSoapFault()) {
                return handleSoapFault(correlationId, maybeRawResponse, maybeResponse.get().toString());
            }
        }

        maybeRawResponse.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_OutboundBeforeTransformation));

        Optional<SoapEnvelope> maybeOasysSOAPResponse;
        try {
            maybeOasysSOAPResponse = offenderTransformer.oasysInitialSearchResponseOf(maybeResponse, maybeOasysInitialSearch);
        }  catch (NDHMappingException ndhme) {
            exceptionLogService.logMappingFail(ndhme.getCode(), ndhme.getSourceValue(), ndhme.getSubject(), correlationId, offenderId);
            return Optional.ofNullable(faultTransformer.mappingSoapFaultOf(ndhme, correlationId));
        }

        Optional<String> maybeXmlResponse = maybeOasysSOAPResponse.map(oasysResponse -> handleOkResponse(correlationId, oasysResponse));

        maybeXmlResponse.ifPresent(xml -> logMessage(correlationId, xml, offenderId, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation));

        return maybeXmlResponse;
    }


    private Optional<SoapEnvelope> deliusInitialSearchResponseOf(Optional<String> maybeRawResponse, String correlationId) {
        return maybeRawResponse.flatMap(rawResponse -> {
            try {
                return Optional.of(xmlMapper.readValue(rawResponse, SoapEnvelope.class));
            } catch (IOException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(rawResponse, correlationId, "Can't deserialize delius initial search response: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<String> rawDeliusInitialSearchResponseOf(Optional<String> maybeTransformedXml, String correlationId) {
        return maybeTransformedXml.flatMap((String transformedXml) -> callDeliusInitialSearch(transformedXml, correlationId));
    }

    private Optional<SoapEnvelope> deliusInitialSearchRequestOf(Optional<SoapEnvelope> maybeOasysInitialSearch) {
        return maybeOasysInitialSearch.map(offenderTransformer::deliusInitialSearchRequestOf);
    }

    private void logMessage(String correlationId, String xml, String offenderId, MessageStoreService.ProcStates procState) {
        messageStoreService.writeMessage(xml, correlationId, offenderId, NDH_WEB_SERVICE_SEARCH, procState);
    }

    private Optional<String> callDeliusInitialSearch(String transformedXml, String correlationId) {
        try {
            return Optional.of(deliusInitialSearchClient.deliusWebServiceResponseOf(transformedXml));
        } catch (UnirestException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(transformedXml, correlationId, "Can't talk to Delius initial search endpoint: " + e.getMessage());
            return Optional.empty();
        }
    }
}
