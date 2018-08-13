package uk.gov.justice.digital.ndh.service.transforms;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.response.GetSubSetOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.InitialSearchResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.SubSetEvent;
import uk.gov.justice.digital.ndh.api.oasys.response.SubSetOffender;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.MappingService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OffenderTransformer {

    public static final long SENTENCE_CODE_TYPE = 3802L;
    private final CommonTransformer commonTransformer;
    private final FaultTransformer faultTransformer;
    private final MappingService mappingService;

    @Autowired
    public OffenderTransformer(CommonTransformer commonTransformer, FaultTransformer faultTransformer, MappingService mappingService) {
        this.commonTransformer = commonTransformer;
        this.faultTransformer = faultTransformer;
        this.mappingService = mappingService;
    }

    public SoapEnvelope deliusInitialSearchRequestOf(SoapEnvelope oasysInitialSearchRequest) {

        final String correlationID = oasysInitialSearchRequest.getBody().getInitialSearchRequest().getHeader().getCorrelationID();
        return SoapEnvelope.builder()
                .header(commonTransformer.deliusSoapHeaderOf(correlationID))
                .body(SoapBody
                        .builder()
                        .getSubSetOffenderEventRequest(getSubsetOffenderEventRequestOf(oasysInitialSearchRequest.getBody().getInitialSearchRequest()))
                        .build())
                .build();
    }

    private GetSubSetOffenderEventRequest getSubsetOffenderEventRequestOf(InitialSearchRequest oasysInitialSearchRequest) {
        return GetSubSetOffenderEventRequest
                .builder()
                .caseReferenceNumber(oasysInitialSearchRequest.getCmsProbNumber())
                .forename1(oasysInitialSearchRequest.getForename1())
                .surname(oasysInitialSearchRequest.getFamilyName())
                .build();
    }

    public SoapEnvelope oasysInitialSearchResponseOf(SoapEnvelope deliusInitialSearchResponse, Optional<SoapEnvelope> maybeOasysInitialSearchRequest) {
        return SoapEnvelope
                .builder()
                .body(
                        SoapBody
                                .builder()
                                .initialSearchResponse(
                                        InitialSearchResponse
                                                .builder()
                                                .header(maybeOasysInitialSearchRequest.map(isr -> isr.getBody().getInitialSearchRequest().getHeader()).orElse(null))
                                                .subSetOffenders(subsetOffendersOf(deliusInitialSearchResponse.getBody().getGetSubSetOffenderDetailsResponse()))
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private List<SubSetOffender> subsetOffendersOf(GetSubSetOffenderDetailsResponse getSubSetOffenderDetailsResponse) {

        final ImmutableList.Builder<SubSetOffender> builder = ImmutableList.builder();

        final uk.gov.justice.digital.ndh.api.delius.response.SubSetOffender subSetOffender = getSubSetOffenderDetailsResponse.getSubSetOffender();
        builder.add(SubSetOffender
                .builder()
                .cmsProbNumber(subSetOffender.getCaseReferenceNumber())
                .dateOfBirth(subSetOffender.getDateOfBirth())
                .familyName(subSetOffender.getLastName())
                .forename1(subSetOffender.getForename1())
                .gender(oasysGenderOf(subSetOffender.getGender()))
                .laoIndicator(subSetOffender.getLaoIndicator())
                .subSetEvents(subsetEventsOf(getSubSetOffenderDetailsResponse.getSubSetEvents()))
                .build());

        return builder.build();
    }

    private String oasysGenderOf(String gender) {
        return Optional.ofNullable(gender).map(this::mapGender).orElse(null);
    }

    private String mapGender(String g) {
        switch (g) {
            case "M":
                return "1";
            case "F":
                return "2";
            default:
                return "9";
        }
    }

    private List<SubSetEvent> subsetEventsOf(List<uk.gov.justice.digital.ndh.api.delius.response.SubSetEvent> subSetEvents) {
        return subSetEvents.stream().map(
                subSetEvent -> SubSetEvent.builder()
                        .eventNumber(subSetEvent.getEventNumber())
                        .sentenceDate(subSetEvent.getCommencementDate())
                        .sentenceCode(sentenceCodeOf(subSetEvent.getOrderType()))
                        .build()
        ).collect(Collectors.toList());
    }

    private String sentenceCodeOf(String orderType) {
        return Optional.ofNullable(orderType)
                .map(ot -> mappingService.descriptionOf(orderType, SENTENCE_CODE_TYPE))
                .orElse(null);
    }
}
