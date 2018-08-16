package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;
import uk.gov.justice.digital.ndh.service.transforms.FaultTransformer;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class OasysOffenderService extends RequestResponseService {
    public static final String NDH_WEB_SERVICE_SEARCH = "NDH_Web_Service_Search";
    private final OffenderTransformer offenderTransformer;
    private final FaultTransformer faultTransformer;
    private final DeliusInitialSearchClient deliusInitialSearchClient;

    @Autowired
    public OasysOffenderService(OffenderTransformer offenderTransformer, CommonTransformer commonTransformer, ExceptionLogService exceptionLogService, MessageStoreService messageStoreService, DeliusInitialSearchClient deliusInitialSearchClient, XmlMapper xmlMapper, FaultTransformer faultTransformer) {
        super(exceptionLogService, commonTransformer, messageStoreService, xmlMapper);
        this.offenderTransformer = offenderTransformer;
        this.deliusInitialSearchClient = deliusInitialSearchClient;
        this.faultTransformer = faultTransformer;
    }

    public Optional<String> initialSearch(String initialSearchXml) {
        val maybeOasysInitialSearch = commonTransformer.asSoapEnvelope(initialSearchXml);

        val correlationId = maybeOasysInitialSearch.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getHeader().getCorrelationID()).orElse(null);
        val offenderId = maybeOasysInitialSearch.map(initialSearch -> initialSearch.getBody().getInitialSearchRequest().getCmsProbNumber()).orElse(null);

        val maybeTransformed = deliusInitialSearchRequestOf(initialSearchXml, maybeOasysInitialSearch, correlationId, offenderId);

        val maybeTransformedXml = stringXmlOf(maybeTransformed, correlationId, offenderId, NDH_WEB_SERVICE_SEARCH);

        val maybeRawResponse = rawDeliusInitialSearchResponseOf(maybeTransformedXml, correlationId);

        val maybeResponse = deliusInitialSearchResponseOf(maybeRawResponse, correlationId, offenderId, NDH_WEB_SERVICE_SEARCH);

        return maybeResponse.flatMap(response -> {
            try {
                return Optional.of(stringResponseOf(response, maybeOasysInitialSearch, maybeRawResponse, correlationId, offenderId, NDH_WEB_SERVICE_SEARCH));
            } catch (DocumentException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(response.toString(), correlationId, "Can't transform fault response: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                exceptionLogService.logFault(response.toString(), correlationId, "Can't serialize transformed risk update response: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public String stringResponseOf(SoapEnvelope response, Optional<SoapEnvelope> maybeOasysInitialSearch, Optional<String> maybeRawResponse, String correlationId, String offenderId, String processName) throws DocumentException, JsonProcessingException {
        if (response.getBody().isSoapFault()) {
            exceptionLogService.logFault(maybeRawResponse.get(), correlationId, "SOAP Fault returned from Delius initialSearch service");
            return faultTransformer.oasysFaultResponseOf(maybeRawResponse.get(), correlationId);
        } else {
            messageStoreService.writeMessage(maybeRawResponse.get(), correlationId, offenderId, processName, MessageStoreService.ProcStates.GLB_ProcState_OutboundBeforeTransformation);
            final SoapEnvelope transformedResponse = offenderTransformer.oasysInitialSearchResponseOf(response, maybeOasysInitialSearch);
            final String transformedResponseXmlOf = commonTransformer.transformedResponseXmlOf(transformedResponse);
            messageStoreService.writeMessage(transformedResponseXmlOf, correlationId, offenderId, processName, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation);
            return transformedResponseXmlOf;
        }
    }


    private Optional<SoapEnvelope> deliusInitialSearchResponseOf(Optional<String> maybeRawResponse, String correlationId, String offenderId, String processName) {
        return maybeRawResponse.flatMap(rawResponse -> {
            messageStoreService.writeMessage(rawResponse, correlationId, offenderId, processName, MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation);
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

    private Optional<SoapEnvelope> deliusInitialSearchRequestOf(String updateXml, Optional<SoapEnvelope> maybeOasysInitialSearch, String correlationId, String offenderId) {
        return maybeOasysInitialSearch.map(oasysInitialSearch -> {
            messageStoreService.writeMessage(updateXml, correlationId, offenderId, NDH_WEB_SERVICE_SEARCH, MessageStoreService.ProcStates.GLB_ProcState_InboundBeforeTransformation);
            return offenderTransformer.deliusInitialSearchRequestOf(oasysInitialSearch);
        });
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
