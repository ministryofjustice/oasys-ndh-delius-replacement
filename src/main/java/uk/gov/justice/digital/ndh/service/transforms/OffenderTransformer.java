package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import uk.gov.justice.digital.ndh.api.nomis.Address;
import uk.gov.justice.digital.ndh.api.nomis.AgencyInternalLocation;
import uk.gov.justice.digital.ndh.api.nomis.AgencyLocation;
import uk.gov.justice.digital.ndh.api.nomis.Alert;
import uk.gov.justice.digital.ndh.api.nomis.Booking;
import uk.gov.justice.digital.ndh.api.nomis.CourtEvent;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.KeyValue;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderAssessment;
import uk.gov.justice.digital.ndh.api.nomis.OffenderImprisonmentStatus;
import uk.gov.justice.digital.ndh.api.nomis.Phone;
import uk.gov.justice.digital.ndh.api.nomis.Physicals;
import uk.gov.justice.digital.ndh.api.nomis.ProfileDetails;
import uk.gov.justice.digital.ndh.api.nomis.Sentence;
import uk.gov.justice.digital.ndh.api.nomis.SentenceCalculation;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
@Slf4j
public class OffenderTransformer {

    public static final long SENTENCE_CODE_TYPE = 3802L;
    public static final long COURT_CODE_TYPE = 3807L;
    public static final long LANGUAGE_CODE_TYPE = 3806L;
    public static final long LICENCE_MAIN_CATEGORY = 3803L;
    public static final long LICENCE_SUB_CATEGORY = 3804L;
    public static final String NO = "NO";
    public static final String YES = "YES";
    public static final long SECURITY_CATEGORY_CODE_TYPE = 13L;
    private static final String ADDITIONAL_SENTENCING_REQUIREMENTS = "ADDITIONAL_SENTENCING_REQUIREMENTS";
    public static final long RELEASE_NAME_CODE_TYPE = 34L;
    public final BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> initialSearchResponseTransform;
    public final BiFunction<Optional<SoapEnvelope>, Optional<SoapEnvelope>, Optional<SoapEnvelope>> offenderDetailsResponseTransform;
    private final CommonTransformer commonTransformer;
    private final MappingService mappingService;
    private final RequirementLookupRepository requirementLookupRepository;
    private final ObjectMapper objectMapper;


    @Autowired
    public OffenderTransformer(CommonTransformer commonTransformer,
                               MappingService mappingService,
                               RequirementLookupRepository requirementLookupRepository,
                               @Qualifier("globalObjectMapper")
                                       ObjectMapper objectMapper) {
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

        this.objectMapper = objectMapper;
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
        return Optional.ofNullable(courtType).map(ct -> mappingService.targetValueOf(courtType, COURT_CODE_TYPE)).orElse(null);
    }

    private String oasysSentenceCodeOf(Event event) {
        return Optional.ofNullable(event.getOrderType()).map(orderType ->
                mappingService.targetValueOf(orderType, SENTENCE_CODE_TYPE)).orElse(null);
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
        final String mainCategory = mappingService.targetValueOf(category.getMainCategory(), LICENCE_MAIN_CATEGORY);

        final Optional<String> maybeTargetSubCategory = Optional.ofNullable(category.getSubCategory()).filter(sc -> sc.equals("99"));
        final Optional<String> maybeSourceSubcategory = maybeTargetSubCategory.map(tsc -> mappingService.targetValueOf(tsc, LICENCE_SUB_CATEGORY));

        return maybeSourceSubcategory
                .map(sc -> Stream.of(mainCategory, sc).collect(Collectors.joining(":")))
                .orElse(mainCategory);
    }

    private String oasysLanguageOf(String language) {
        return Optional.ofNullable(language).map(lang -> mappingService.targetValueOf(language, LANGUAGE_CODE_TYPE)).orElse(null);
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

    public String oasysGenderOf(String gender) {
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

    public SoapEnvelope oasysOffenderDetailResponseOf(Optional<Offender> maybeOffender, Booking latestBooking, Optional<SentenceCalculation> maybeSentenceCalc, Optional<Sentence> maybeSentence, Optional<OffenderImprisonmentStatus> maybeImprisonmentStatus, Optional<List<CourtEvent>> maybeCourtEvents, Optional<AgencyLocation> sentencingCourtAgencyLocation, Optional<Address> maybeHomeAddress, Optional<Address> maybeDischargeAddress, Optional<List<Physicals>> maybePhysicals, Optional<List<OffenderAssessment>> maybeAssessments, Optional<List<Alert>> maybeF2052Alerts, Offender offender) {
        return SoapEnvelope
                .builder()
                .header(SoapHeader.builder().build())
                .body(SoapBody
                        .builder()
                        .offenderDetailsResponse(OffenderDetailsResponse
                                .builder()
                                .offenderDetail(
                                        OffenderDetail
                                                .builder()
                                                .prisonNumber(Optional.ofNullable(latestBooking.getBookingNo()).orElse(null))
                                                .nomisId(offender.getNomsId())
                                                .familyName(offender.getSurname())
                                                .forename1(offender.getFirstName())
                                                .forename2(forename2Of(offender.getMiddleNames()))
                                                .forename3(forename3Of(offender.getMiddleNames()))
                                                .gender(oasysGenderOf(offender.getGender().getCode()))
                                                .dateOfBirth(XMLFormattedDateOf(offender.getDateOfBirth()))
                                                .pnc(pncOf(offender.getIdentifiers()))
                                                .releaseDate(releaseDateOf(maybeSentenceCalc))
                                                .releaseType(maybeSentenceCalc.map(SentenceCalculation::getReleaseType).orElse(null))
                                                .cellLocation(Optional.ofNullable(latestBooking.getLivingUnit()).map(AgencyInternalLocation::getDescription).orElse(null))
                                                .appealPendingIndicator(appealPendingOf(maybeCourtEvents, latestBooking.getBookingId()))
                                                .curfewDate(curfewDateOf(maybeSentenceCalc))
                                                .licenceExpiryDate(licenceExpiryDateOf(maybeSentenceCalc))
                                                .sentenceExpiryDate(sentenceExpiryDateOf(maybeSentenceCalc))
                                                .conditionalReleaseDate(conditionalReleaseDateOf(maybeSentenceCalc))

                                                .addressLine1(maybeHomeAddress.map(Address::getFlat).orElse(null))
                                                .addressLine2(maybeHomeAddress.map(Address::getPremise).orElse(null))
                                                .addressLine3(maybeHomeAddress.map(Address::getStreet).orElse(null))
                                                .addressLine4(maybeHomeAddress.map(Address::getLocality).orElse(null))
                                                .addressLine5(maybeHomeAddress.flatMap(a -> Optional.ofNullable(a.getCity())).map(KeyValue::getDescription).orElse(null))
                                                .addressLine6(maybeHomeAddress.map(Address::getCountyCode).orElse(null))
                                                .postCode(maybeHomeAddress.map(Address::getPostalCode).orElse(null))
                                                .telephoneNumber(telephoneOf(maybeHomeAddress, "HOME"))

                                                .dischargeAddressLine1(maybeDischargeAddress.map(Address::getFlat).orElse(null))
                                                .dischargeAddressLine2(maybeDischargeAddress.map(Address::getPremise).orElse(null))
                                                .dischargeAddressLine3(maybeDischargeAddress.map(Address::getStreet).orElse(null))
                                                .dischargeAddressLine4(maybeDischargeAddress.map(Address::getLocality).orElse(null))
                                                .dischargeAddressLine5(maybeDischargeAddress.flatMap(a -> Optional.ofNullable(a.getCity())).map(KeyValue::getDescription).orElse(null))
                                                .dischargeAddressLine6(maybeDischargeAddress.map(Address::getCountyCode).orElse(null))
                                                .dischargePostCode(maybeDischargeAddress.map(Address::getPostalCode).orElse(null))
                                                .dischargeTelephoneNumber(telephoneOf(maybeDischargeAddress, "HOME"))

                                                .aliases(oasysAliasesOf(maybeOffender))
                                                .religion(religionCodeOf(maybePhysicals))
                                                .croNumber(identifierOf(maybeOffender, "CRO"))
                                                .pnc(identifierOf(maybeOffender, "PNC"))
                                                .riskOfSelfHarm(f2052AlertOf(maybeF2052Alerts))
                                                .securityCategory(securityCategoryOf(maybeAssessments))
                                                .releaseType(releaseTypeOf(maybeSentenceCalc))
                                                .build()
                                )
                                .eventDetail(
                                        EventDetail
                                                .builder()
                                                .sentenceCode(sentenceCodeOf(maybeSentenceCalc, maybeImprisonmentStatus, maybeSentence))
                                                .sentenceDate(sentenceDateOf(maybeSentence))
                                                .sentenceLength(sentenceLengthInDaysOf(maybeSentence, maybeSentenceCalc).map(Object::toString).orElse(null))
                                                .courtCode(sentencingCourtAgencyLocation.map(AgencyLocation::getAgencyLocationId).orElse(null))
                                                .build()
                                )
                                .build())
                        .build())
                .build();
    }

    private String releaseTypeOf(Optional<SentenceCalculation> maybeSentenceCalc) {
        return maybeSentenceCalc.map(SentenceCalculation::getReleaseType)
                .map(rt -> mappingService.targetValueOf(rt, RELEASE_NAME_CODE_TYPE))
                .orElse(null);
    }

    private String f2052AlertOf(Optional<List<Alert>> maybeF2052Alerts) {
        return maybeF2052Alerts.map(alerts -> alerts.isEmpty() ? NO : YES).orElse(NO);
    }

    private String securityCategoryOf(Optional<List<OffenderAssessment>> maybeAssessments) {
        return maybeAssessments.flatMap(assessments -> assessments
                .stream()
                .filter(a -> "A".equals(a.getAssessStatus()) &&
                        "TYPE".equals(a.getAssessmentType().getAssessmentClass()) &&
                        "CATEGORY".equals(a.getAssessmentType().getAssessmentCode()))
                .findFirst()
                .map(x -> x.getReviewSupLevelType().getCode())
                .map(sc -> "Z".equals(sc) ? "Z" : mappingService.targetValueOf(sc, SECURITY_CATEGORY_CODE_TYPE))).orElse(null);
    }

    private String forename2Of(String middleNames) {
        return Optional.ofNullable(middleNames).map(mns -> mns.split(" ")[0]).orElse(null);
    }

    private String forename3Of(String middleNames) {
        return Optional.ofNullable(forename2Of(middleNames)).map(fn2 -> middleNames.replaceFirst(fn2, "").trim()).filter(s -> !s.isEmpty()).orElse(null);
    }

    public Offender asOffender(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, Offender.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return Offender.builder().build();
        }
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

    private String conditionalReleaseDateOf(Optional<SentenceCalculation> maybeSentenceCalc) {
        return maybeSentenceCalc.flatMap(sc -> firstNonNullDateOf(sc.getCrdOverridedDate(), sc.getCrdCalculatedDate())).map(LocalDateTime::toString).orElse(null);
    }

    private String sentenceExpiryDateOf(Optional<SentenceCalculation> maybeSentenceCalc) {
        return maybeSentenceCalc.flatMap(sc -> firstNonNullDateOf(sc.getSedOverridedDate(), sc.getSedCalculatedDate())).map(LocalDateTime::toString).orElse(null);
    }

    private String licenceExpiryDateOf(Optional<SentenceCalculation> maybeSentenceCalc) {
        return maybeSentenceCalc.flatMap(sc -> firstNonNullDateOf(sc.getLedOverridedDate(), sc.getLedCalculatedDate())).map(LocalDateTime::toString).orElse(null);
    }

    private String curfewDateOf(Optional<SentenceCalculation> maybeSentenceCalc) {
        return maybeSentenceCalc.flatMap(sc -> firstNonNullDateOf(sc.getHdcadOverridedDate(), sc.getHdcadCalculatedDate())).map(LocalDateTime::toString).orElse(null);
    }

    public Optional<LocalDateTime> firstNonNullDateOf(LocalDateTime a, LocalDateTime b) {
        return Optional.ofNullable(Optional.ofNullable(a).orElse(b));
    }

    private String appealPendingOf(Optional<List<CourtEvent>> maybeCourtEvents, Long bookingId) {
        return maybeCourtEvents.flatMap(
                courtEvents -> courtEvents
                        .stream()
                        .filter(ce -> ce.getBookingId().equals(bookingId))
                        .max(Comparator.comparing(CourtEvent::getStartDateTime))
                        .map(CourtEvent::getComments)
                        .map(str -> ("APPEAL".equals(str)) ? "Y" : "N")
        ).orElse("N");
    }

    private String releaseDateOf(Optional<SentenceCalculation> maybeSentenceCalc) {
        return maybeSentenceCalc.flatMap(
                sc -> Optional.ofNullable(sc.getReleaseDate()))
                .map(LocalDate::toString)
                .orElse(null);
    }

    private String telephoneOf(Optional<Address> maybeHomeAddress, String phoneType) {
        return maybeHomeAddress.flatMap(a -> Optional.ofNullable(a.getPhones()))
                .flatMap(ps -> ps
                        .stream()
                        .filter(p -> phoneType.equals(p.getPhoneType()))
                        .findFirst())
                .map(Phone::getPhoneNo).orElse(null);
    }

    private List<Alias> oasysAliasesOf(Optional<Offender> maybeOffender) {
        return maybeOffender.flatMap(offender -> Optional.ofNullable(offender.getAliases()))
                .map(aliases -> aliases
                        .stream()
                        .map(alias -> Alias.builder()
                                .aliasFamilyName(alias.getSurname())
                                .aliasForename1(alias.getFirstName())
                                .aliasForename2(forename2Of(alias.getMiddleNames()))
                                .aliasForename3(forename3Of(alias.getMiddleNames()))
                                .aliasDateOfBirth(alias.getDateOfBirth().toString())
                                .build())
                        .collect(Collectors.toList())).orElse(null);
    }


    private String identifierOf(Optional<Offender> maybeOffender, String type) {
        return maybeOffender.flatMap(offender -> offender.getIdentifiers()
                .stream()
                .filter(identifier -> type.equals(identifier.getIdentifierType()))
                .findFirst()
                .map(Identifier::getIdentifier))
                .orElse(null);
    }

    private String religionCodeOf(Optional<List<Physicals>> maybePhysicals) {
        return maybePhysicals
                .map(physicals -> physicals
                        .stream()
                        .flatMap(p -> Optional.ofNullable(p.getProfileDetails())
                                .map(Collection::stream)
                                .orElse(Stream.empty()))
                        .filter(pd -> "RELF".equals(pd.getProfileType()))
                        .findFirst()
                        .map(ProfileDetails::getProfileCode)
                        .orElse(null)
                )
                .orElse(null);
    }

    private String sentenceCodeOf
            (Optional<SentenceCalculation> maybeSentenceCalc, Optional<OffenderImprisonmentStatus> maybeImprisonmentStatus, Optional<Sentence> maybeSentence) {
        if (maybeImprisonmentStatus.isPresent()) {
            switch (maybeImprisonmentStatus.get().getImprisonmentStatus().getImprisonmentStatus()) {
                case "SENT03":
                case "SENTCJ03":
                    return "920";
                case "IPP":
                    return "930";
                case "EXSENT03":
                    return "940";
                case "ALP":
                case "CFLIFE":
                case "DFL":
                case "DLP":
                case "DPP":
                case "HMPL":
                case "LIFE":
                case "LR_DPP":
                case "LR_LIFE":
                case "MLP":
                    return "310";
            }
        }

        if (maybeSentenceCalc.map(SentenceCalculation::getEffectiveSentenceLength).isPresent()) {
            if (maybeSentenceCalc.map(SentenceCalculation::getEffectiveSentenceLength).get().startsWith("99")) {
                return "310";
            }
        }

        if (sentenceLengthInDaysOf(maybeSentence, maybeSentenceCalc).isPresent()) {
            final val days = sentenceLengthInDaysOf(maybeSentence, maybeSentenceCalc).get();
            if (days < 366) {
                return "220";
            } else if (days < 1461) {
                return "200";
            } else if (days < 36135) {
                return "210";
            }
        }

        return "310";
    }

    private Optional<Long> sentenceLengthInDaysOf
            (Optional<Sentence> maybeSentence, Optional<SentenceCalculation> maybeSentenceCalc) {

        return maybeSentence
                .map(Sentence::getStartDate)
                .flatMap(startDate -> maybeSentenceCalc.map(sc -> DAYS.between(startDate, sc.getReleaseDate())));
    }

    private String sentenceDateOf(Optional<Sentence> maybeSentence) {
        return maybeSentence.flatMap(s -> Optional.ofNullable(s.getStartDate())).map(LocalDate::toString).orElse(null);
    }


}
