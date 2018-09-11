package uk.gov.justice.digital.ndh.service.transforms;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.GetOffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.response.Custody;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.delius.response.Event;
import uk.gov.justice.digital.ndh.api.delius.response.GetSubSetOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.OffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.Alias;
import uk.gov.justice.digital.ndh.api.oasys.response.EventDetail;
import uk.gov.justice.digital.ndh.api.oasys.response.InitialSearchResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.Offences;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetail;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.SentenceDetail;
import uk.gov.justice.digital.ndh.api.oasys.response.SubSetEvent;
import uk.gov.justice.digital.ndh.api.oasys.response.SubSetOffender;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookup;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookupRepository;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.exception.NDHRequirementLookupException;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
public class OffenderTransformer {

    public static final long SENTENCE_CODE_TYPE = 3802L;
    public final BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> initialSearchResponseTransform;
    public final BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> offenderDetailsResponseTransform;
    private final CommonTransformer commonTransformer;
    private final MappingService mappingService;
    private final RequirementLookupRepository requirementLookupRepository;

    @Autowired
    public OffenderTransformer(CommonTransformer commonTransformer, MappingService mappingService, RequirementLookupRepository requirementLookupRepository) {
        this.commonTransformer = commonTransformer;
        this.mappingService = mappingService;
        this.requirementLookupRepository = requirementLookupRepository;

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
        return Optional.ofNullable(deliusOffenderDetailsResponse)
                .flatMap(deliusResponse -> Optional.ofNullable(deliusResponse.getEvent()))
                .map(event -> EventDetail
                        .builder()
                        .eventNumber(event.getEventNumber())
                        .offences(oasysOffencesOf(event))
                        .sentenceCode(oasysSentenceCodeOf(event))
                        .sentenceDate(event.getCommencementDate())
                        .offenceDate(event.getOffenceDate())
                        .sentenceLength(event.getOrderLength())
                        .combinedLength(event.getUwHours())
                        .courtCode(event.getCourt())
                        .courtType(oasysCourtTypeOf(event.getCourtType()))
                        .sentenceDetails(oasysSentenceDetailsOf(event))
                        .build())
                .orElse(null);
    }

    private List<SentenceDetail> oasysSentenceDetailsOf(Event event) throws NDHRequirementLookupException {
        return Optional.ofNullable(event).flatMap(e -> Optional.ofNullable(e.getRequirements()))
                .map(requirements -> requirements
                        .stream()
                        .map(requirement -> {
                                    final Optional<RequirementLookup> maybeMapped = requirementLookupRepository.findByReqTypeAndReqCodeAndSubCode("N", requirement.getMainCategory(), requirement.getSubCategory());
                                    return maybeMapped
                                            .map(mapped ->
                                                    SentenceDetail
                                                            .builder()
                                                            .attributeCategory(mapped.getSentenceAttributeCat())
                                                            .attributeElement(mapped.getSentenceAttributeElm())
                                                            .description(mapped.getActivityDesc())
                                                            //TODO:
                                                            .lengthInMonths("?")
                                                            .lengthInHours("?")
                                                            .build())
                                            .orElseThrow(() -> NDHRequirementLookupException
                                                    .builder()
                                                    .reqType("N")
                                                    .reqCode(requirement.getMainCategory())
                                                    .subCode(requirement.getSubCategory())
                                                    .build());
                        })
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private String oasysCourtTypeOf(String courtType) {
        //TODO:
        return null;
    }

    private String oasysSentenceCodeOf(Event event) {
        return Optional.ofNullable(event.getOrderType()).map(orderType ->
                mappingService.targetValueOf(orderType, 3802L)
        ).orElse(null);
    }

    private List<Offences> oasysOffencesOf(Event event) {
        return Optional.ofNullable(event.getOffenceCode())
                .map(offenceCode -> ImmutableList.of(Offences
                        .builder()
                        .offenceGroupCode(offenceCode.substring(0, 3))
                        .offenceSubCode(offenceCode.substring(3, 6))
                        .build()
                ))
                .orElse(null);
    }

    private OffenderDetail oasysOffenderDetailOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
        return Optional.ofNullable(deliusOffenderDetailsResponse)
                .flatMap(deliusResponse -> Optional.ofNullable(deliusResponse.getOffender()))
                .map(offender -> OffenderDetail
                        .builder()
                        .cmsProbNumber(offender.getCaseReferenceNumber())
                        .familyName(offender.getLastName())
                        .forename1(offender.getForename1())
                        .forename2(offender.getForename2())
                        .forename3(offender.getForename3())
                        .gender(oasysGenderOf(offender.getGender()))
                        .dateOfBirth(offender.getDateOfBirth())
                        .aliases(oasysAliasesOf(offender.getAliases()))
                        .ethnicCategory(offender.getEthnicity())
                        .addressLine1(offender.getMainAddress().getAddressFirstLine().getHouseNumber())
                        .addressLine2(offender.getMainAddress().getAddressFirstLine().getBuildingName())
                        .addressLine3(offender.getMainAddress().getStreetName())
                        .addressLine4(offender.getMainAddress().getDistrict())
                        .addressLine5(offender.getMainAddress().getTownOrCity())
                        .addressLine6(offender.getMainAddress().getCounty())
                        .postCode(offender.getPostcode())
                        .telephoneNumber(offender.getTelephone())
                        .pnc(offender.getPoliceNationalComputerIdentifier())
                        .croNumber(offender.getCro())
                        .language(oasysLanguageOf(offender.getLanguage()))
                        .religion(offender.getReligion())
                        .releaseDate(releaseDateOf(deliusOffenderDetailsResponse))
                        .license(oasysLicenseOf(deliusOffenderDetailsResponse))
                        .laoIndicator(offender.getLaoIndicator())
                        .build())
                .orElse(null);
    }

    private String oasysLicenseOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
        //TODO:
        return null;
    }

    private String oasysLanguageOf(String language) {
        //TODO:
        return null;
    }

    private String releaseDateOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
        return Optional.ofNullable(deliusOffenderDetailsResponse.getEvent())
                .flatMap(event -> Optional.ofNullable(event.getCustody()))
                .map(Custody::getReleaseDate)
                .orElse(null);
    }

    private List<Alias> oasysAliasesOf(List<String> aliases) {
        return aliases
                .stream()
                .map(a -> Alias.builder().aliasFamilyName(a).build())
                .collect(Collectors.toList());
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
                        .sentenceCode(oasysSentenceDescriptionOf(subSetEvent.getOrderType()))
                        .build()
        ).collect(Collectors.toList());
    }

    private String oasysSentenceDescriptionOf(String orderType) {
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
                .build();
    }
}
