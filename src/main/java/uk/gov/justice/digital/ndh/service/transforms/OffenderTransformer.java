package uk.gov.justice.digital.ndh.service.transforms;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.GetOffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.delius.response.GetSubSetOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.OffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.EventDetail;
import uk.gov.justice.digital.ndh.api.oasys.response.InitialSearchResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetail;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.SubSetEvent;
import uk.gov.justice.digital.ndh.api.oasys.response.SubSetOffender;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.MappingService;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
public class OffenderTransformer {

    public static final long SENTENCE_CODE_TYPE = 3802L;

    private final CommonTransformer commonTransformer;
    private final MappingService mappingService;
    public final BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> initialSearchResponseTransform;
    public final BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> offenderDetailsResponseTransform;

    @Autowired
    public OffenderTransformer(CommonTransformer commonTransformer, FaultTransformer faultTransformer, MappingService mappingService) {
        this.commonTransformer = commonTransformer;
        this.mappingService = mappingService;
        initialSearchResponseTransform = (rq, rs) -> rs.map(deliusInitialSearchResponse ->
                SoapEnvelope
                        .builder()
                        .body(
                                SoapBody
                                        .builder()
                                        .initialSearchResponse(
                                                InitialSearchResponse
                                                        .builder()
                                                        .header(rq.map(isr -> commonTransformer.oasysHeaderOf(isr.getBody().getInitialSearchRequest().getHeader())).orElse(null))
                                                        .subSetOffenders(subsetOffendersOf(deliusInitialSearchResponse.getBody().getGetSubSetOffenderDetailsResponse()))
                                                        .build()
                                        ).build()
                        )
                        .build());

        offenderDetailsResponseTransform = (rq, rs) -> rs.map(deliusOffenderDetailsResponse ->
                SoapEnvelope
                        .builder()
                        .body(
                                SoapBody
                                        .builder()
                                        .offenderDetailsResponse(
                                                OffenderDetailsResponse
                                                        .builder()
                                                        .header(rq.map(isr -> commonTransformer.oasysHeaderOf(isr.getBody().getInitialSearchRequest().getHeader())).orElse(null))
                                                        .offenderDetail(oasysOffenderDetailOf(deliusOffenderDetailsResponse.getBody().getDeliusOffenderDetailsResponse()))
                                                        .eventDetail(oasysEventDetailOf(deliusOffenderDetailsResponse.getBody().getDeliusOffenderDetailsResponse()))
                                                        .build()
                                        ).build()
                        )
                        .build());

    }

    private EventDetail oasysEventDetailOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
        //TODO:
        return EventDetail.builder().build();
    }

    private OffenderDetail oasysOffenderDetailOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
        //TODO:
        return OffenderDetail.builder().build();
    }

    public SoapEnvelope deliusInitialSearchRequestOf(SoapEnvelope oasysInitialSearchRequest) {

        final String correlationID = oasysInitialSearchRequest.getBody().getInitialSearchRequest().getHeader().getCorrelationID().trim();
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
                .map(ot -> mappingService.descriptionOf(ot, SENTENCE_CODE_TYPE))
                .orElse(null);
    }

    public SoapEnvelope deliusOffenderDetailsRequestOf(SoapEnvelope oasysOffenderDetailRequest) {
        final String correlationID = oasysOffenderDetailRequest.getBody().getOffenderDetailsRequest().getHeader().getCorrelationID().trim();

        return SoapEnvelope
                .builder()
                .header(commonTransformer.deliusSoapHeaderOf(correlationID))
                .body(SoapBody
                        .builder()
                        .getOffenderDetailsRequest(getOffenderDetailsRequestOf(
                                oasysOffenderDetailRequest.getBody().getOffenderDetailsRequest()))
                        .build()
                )
                .build();
    }

    private GetOffenderDetailsRequest getOffenderDetailsRequestOf(OffenderDetailsRequest oasysRequest) {
        return GetOffenderDetailsRequest
                .builder()
                .caseReferenceNumber(oasysRequest.getCmsProbNumber())
                .eventNumber(oasysRequest.getEventNumber())
//                .notesId(oasysRequest.getHeader().)
                .build();
    }
}
