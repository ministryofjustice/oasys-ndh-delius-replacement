package uk.gov.justice.digital.ndh.service.transforms;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.GetOffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.response.AddressFirstLine;
import uk.gov.justice.digital.ndh.api.delius.response.Category;
import uk.gov.justice.digital.ndh.api.delius.response.Custody;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.delius.response.Event;
import uk.gov.justice.digital.ndh.api.delius.response.GetSubSetOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.delius.response.RequirementDetails;
import uk.gov.justice.digital.ndh.api.delius.response.Type;
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
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookup;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookupRepository;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.exception.NDHRequirementLookupException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OffenderTransformer {

    public static final long SENTENCE_CODE_TYPE = 3802L;
    public static final long COURT_CODE_TYPE = 5507L;
    public static final long LANGUAGE_CODE_TYPE = 3806L;
    public static final long LICENCE_MAIN_CATEGORY = 3803L;
    public static final long LICENCE_SUB_CATEGORY = 3804L;
    private static final String ADDITIONAL_SENTENCING_REQUIREMENTS = "ADDITIONAL_SENTENCING_REQUIREMENTS";
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
                        .header(SoapHeader.builder().build())
                        .body(
                                SoapBody
                                        .builder()
                                        .offenderDetailsResponse(
                                                OffenderDetailsResponse
                                                        .builder()
                                                        .header(rq.map(isr -> commonTransformer.oasysHeaderOf(isr.getBody().getOffenderDetailsRequest().getHeader())).orElse(null))
                                                        .offenderDetail(oasysOffenderDetailOf(deliusOffenderDetailsResponse.getBody().getDeliusOffenderDetailsResponse()))
                                                        .eventDetail(oasysEventDetailOf(deliusOffenderDetailsResponse.getBody().getDeliusOffenderDetailsResponse()))
                                                        .build()
                                        ).build()
                        )
                        .build());

    }

    private EventDetail oasysEventDetailOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
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
        final List<SentenceDetail> requirementSentenceDetails = Optional.ofNullable(event).flatMap(e -> Optional.ofNullable(e.getRequirements()))
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
                                                    .lengthInMonths(lengthInMonthsOf(mapped, requirement))
                                                    .lengthInHours(lengthInHoursOf(mapped, requirement))
                                                    .build())
                                    .orElseThrow(() -> NDHRequirementLookupException
                                            .builder()
                                            .reqType("N")
                                            .reqCode(requirement.getMainCategory())
                                            .subCode(requirement.getSubCategory())
                                            .build());
                        })
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        final List<SentenceDetail> additionalRequirementSentenceDetails = Optional.ofNullable(event).flatMap(e -> Optional.ofNullable(e.getAdditionalRequirements()))
                .map(requirements -> requirements
                        .stream()
                        .map(requirement ->
                                SentenceDetail
                                        .builder()
                                        .attributeCategory(ADDITIONAL_SENTENCING_REQUIREMENTS)
                                        .attributeElement(requirement.getMainCategory())
                                        .build())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return ImmutableList.<SentenceDetail>builder()
                .addAll(requirementSentenceDetails)
                .addAll(additionalRequirementSentenceDetails)
                .build();
    }

    private String lengthInHoursOf(RequirementLookup mapped, Category requirement) {
        return Optional.ofNullable(mapped.getCjaUnpaidHours()).
                map(hours -> Optional.ofNullable(requirement.getRequirementDetails())
                        .map(RequirementDetails::getLength)
                        .orElse(null))
                .orElse(null);

    }

    private String lengthInMonthsOf(RequirementLookup mapped, Category requirement) {
        return Optional.ofNullable(mapped.getCjaSupervisionMonths()).
                map(months -> Optional.ofNullable(requirement.getRequirementDetails())
                        .map(RequirementDetails::getLength)
                        .orElse(null))
                .orElse(null);
    }

    private String oasysCourtTypeOf(String courtType) {
        return Optional.ofNullable(courtType).map(ct -> mappingService.sourceValueOf(courtType, COURT_CODE_TYPE)).orElse(null);
    }

    private String oasysSentenceCodeOf(Event event) {
        return Optional.ofNullable(event.getOrderType()).map(orderType ->
                mappingService.sourceValueOf(orderType, SENTENCE_CODE_TYPE)).orElse(null);
    }

    private List<Offences> oasysOffencesOf(Event event) {
        return Optional.ofNullable(event.getOffenceCode())
                .map(offenceCode -> ImmutableList.of(Offences
                        .builder()
                        .offenceGroupCode(offenceCode.substring(0, 3))
                        .offenceSubCode(offenceCode.substring(3, 5))
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
                        .ethnicCategory(Optional.ofNullable(offender.getEthnicity()).orElse(""))
                        .addressLine1(Optional.ofNullable(offender.getMainAddress()).flatMap(addr -> Optional.ofNullable(addr.getAddressFirstLine())).map(AddressFirstLine::getHouseNumber).orElse(null))
                        .addressLine2(Optional.ofNullable(offender.getMainAddress()).flatMap(addr -> Optional.ofNullable(addr.getAddressFirstLine())).map(AddressFirstLine::getBuildingName).orElse(null))
                        .addressLine3(Optional.ofNullable(offender.getMainAddress()).flatMap(addr -> Optional.ofNullable(addr.getStreetName())).orElse(null))
                        .addressLine4(Optional.ofNullable(offender.getMainAddress()).flatMap(addr -> Optional.ofNullable(addr.getDistrict())).orElse(null))
                        .addressLine5(Optional.ofNullable(offender.getMainAddress()).flatMap(addr -> Optional.ofNullable(addr.getTownOrCity())).orElse(null))
                        .addressLine6(Optional.ofNullable(offender.getMainAddress()).flatMap(addr -> Optional.ofNullable(addr.getCounty())).orElse(null))
                        .postCode(offender.getPostcode())
                        .telephoneNumber(Optional.ofNullable(offender.getTelephone()).orElse(""))
                        .pnc(offender.getPoliceNationalComputerIdentifier())
                        .croNumber(offender.getCro())
                        .language(Optional.ofNullable(oasysLanguageOf(offender.getLanguage())).orElse(""))
                        .religion(offender.getReligion())
                        .releaseDate(releaseDateOf(deliusOffenderDetailsResponse))
                        .license(oasysLicenseOf(deliusOffenderDetailsResponse))
                        .laoIndicator(offender.getLaoIndicator())
                        .build())
                .orElse(null);
    }

    private String oasysLicenseOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {

        val categories =
                Optional.ofNullable(deliusOffenderDetailsResponse.getEvent())
                        .flatMap(e -> Optional.ofNullable(e.getCustody()))
                        .flatMap(c -> Optional.ofNullable(c.getLicenceConditions()))
                        .map(licenceConditions -> licenceConditions
                                .stream()
                                .flatMap(licenceCondition -> licenceCondition.getTypes()
                                        .stream()
                                        .filter(type -> Optional.ofNullable(type.getPostCJALicenceConditionType()).isPresent())
                                        .map(Type::getPostCJALicenceConditionType))
                                .collect(Collectors.toList())).orElse(Collections.emptyList());

        final val licenceStr = categories
                .stream()
                .map(this::oasysLicenceStringOf)
                .collect(Collectors.joining(","));

        return (licenceStr.isEmpty()) ? null : licenceStr;
    }

    private String oasysLicenceStringOf(Category category) {
        final String mainCategory = mappingService.sourceValueOf(category.getMainCategory(), LICENCE_MAIN_CATEGORY);

        final Optional<String> maybeTargetSubCategory = Optional.ofNullable(category.getSubCategory()).filter(sc -> sc.equals("99"));
        final Optional<String> maybeSourceSubcategory = maybeTargetSubCategory.map(tsc -> mappingService.sourceValueOf(tsc, LICENCE_SUB_CATEGORY));

        return maybeSourceSubcategory
                .map(sc -> Stream.of(mainCategory, sc).collect(Collectors.joining(":")))
                .orElse(mainCategory);
    }

    private String oasysLanguageOf(String language) {
        return Optional.ofNullable(language).map(lang -> mappingService.sourceValueOf(language, LANGUAGE_CODE_TYPE)).orElse(null);
    }

    private String releaseDateOf(DeliusOffenderDetailsResponse deliusOffenderDetailsResponse) {
        return Optional.ofNullable(deliusOffenderDetailsResponse.getEvent())
                .flatMap(event -> Optional.ofNullable(event.getCustody()))
                .map(Custody::getReleaseDate)
                .orElse(null);
    }

    private List<Alias> oasysAliasesOf(List<String> aliases) {
        return Optional.ofNullable(aliases).map(as -> as
                .stream()
                .map(a -> Alias.builder().aliasFamilyName(a).build())
                .collect(Collectors.toList()))
                .orElse(null);
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
