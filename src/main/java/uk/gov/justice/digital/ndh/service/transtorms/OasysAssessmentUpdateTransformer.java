package uk.gov.justice.digital.ndh.service.transtorms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusAssessmentUpdateSoapBody;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusAssessmentUpdateSoapHeader;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRequest;
import uk.gov.justice.digital.ndh.api.delius.request.OasysAssessmentSummary;
import uk.gov.justice.digital.ndh.api.delius.request.OasysCommonHeader;
import uk.gov.justice.digital.ndh.api.delius.request.OasysSupervisionPlan;
import uk.gov.justice.digital.ndh.api.delius.request.RiskType;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.Assessment;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.NdhAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.oasys.request.Objective;
import uk.gov.justice.digital.ndh.api.oasys.request.Risk;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeDataPK;
import uk.gov.justice.digital.ndh.jpa.repository.MappingRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OasysAssessmentUpdateTransformer {

    private static final String UNMAPPED = "XXXX";
    private static final long OASYS_CRMS_CRIM_NEED = 5500L;
    private static final long OASYSRCMS_OBJ_STATUS_CODE = 5501L;
    private static final long OASYSRCMS_INTERVENTION = 5506L;
    private static final long OASYSRPCMS_COURTTYPE = 5507L;
    private static final long OASYSRPCMS_LAYER1OBJ = 5504L;

    private final MappingRepository mappingRepository;
    private Function<String, String> deliusLayerOf = part -> "".equals(part) ? "" : targetValueOf(part, OASYSRPCMS_LAYER1OBJ);
    private Function<String, String> deliusRiskFlagOf = part -> "".equals(part) ? "L" : part;
    private Function<String, String> deliusConcernFlagOf = part -> {
        String mapped;
        switch (part) {
            case "YES":
                mapped = "YES";
                break;
            case "NO":
                mapped = "NO";
                break;
            default:
                mapped = "DK";
        }
        return mapped;
    };

    @Autowired
    public OasysAssessmentUpdateTransformer(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public DeliusRequest deliusAssessmentUpdateOf(NdhAssessmentUpdateSoapEnvelope ndhSoapEnvelope) {

        return DeliusRequest.builder()
                .header(DeliusAssessmentUpdateSoapHeader
                        .builder()
                        .commonHeader(deliusHeaderOf(ndhSoapEnvelope.getBody().getCmsUpdate().getHeader()))
                        .build())
                .body(DeliusAssessmentUpdateSoapBody
                        .builder()
                        .request(SubmitAssessmentSummaryRequest
                                .builder()
                                //TODO: May have to guard against NPEs for the following. Will every update have an Assessment??
                                .oasysAssessmentSummary(deliusOasysAssessmentSummaryOf(ndhSoapEnvelope.getBody().getCmsUpdate().getAssessment()))
                                .oasysSupervisionPlans(deliusSupervisionPlansOf(ndhSoapEnvelope.getBody().getCmsUpdate()))
                                .riskType(deliusRiskOf(ndhSoapEnvelope.getBody().getCmsUpdate().getAssessment().getRisk(), ndhSoapEnvelope.getBody().getCmsUpdate().getAssessment()))
                                .build())
                        .build())
                .build();
    }

    private OasysCommonHeader deliusHeaderOf(Header ndhOasysHeader) {
        return Optional.ofNullable(ndhOasysHeader).map(
                header -> OasysCommonHeader.builder()
                        //TODO:
                        //.messageId(ndhOasysHeader.getXYZ())
                        // etc
                        .build()
        ).orElse(null);
    }

    private RiskType deliusRiskOf(Risk ndhRisk, Assessment assessment) {
        return Optional.ofNullable(ndhRisk).map(
                risk -> RiskType.builder()
                        .caseReferenceNumber(assessment.getCmsProbNumber())
                        .riskOfHarm(deliusRiskFlagsOf(ndhRisk.getRiskofHarm()))
                        .build()).orElse(null);

    }

    private List<OasysSupervisionPlan> deliusSupervisionPlansOf(CmsUpdate cmsUpdate) {
        return Optional.ofNullable(cmsUpdate.getObjectives())
                .map(objectives -> objectives.stream()
                        .map(objective -> deliusSupervisionPlanOf(objective, cmsUpdate.getAssessment()))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private OasysSupervisionPlan deliusSupervisionPlanOf(Objective objective, Assessment assessment) {
        return OasysSupervisionPlan.builder()
                .caseReferenceNumber(assessment.getCmsProbNumber())
                .oasysId(assessment.getAssessmentGUID())
                .objectiveNumber(objective.getObjectiveNumber())
                .need1(Optional.ofNullable(descriptionOf(objective.getNeed1(), OASYS_CRMS_CRIM_NEED)).map(result -> result.substring(1, 50)).orElse(null))
                .need2(Optional.ofNullable(descriptionOf(objective.getNeed2(), OASYS_CRMS_CRIM_NEED)).map(result -> result.substring(1, 50)).orElse(null))
                .need3(Optional.ofNullable(descriptionOf(objective.getNeed3(), OASYS_CRMS_CRIM_NEED)).map(result -> result.substring(1, 50)).orElse(null))
                .need4(Optional.ofNullable(descriptionOf(objective.getNeed4(), OASYS_CRMS_CRIM_NEED)).map(result -> result.substring(1, 50)).orElse(null))
                .objective(objective.getObjectiveDescription().substring(1, 50)) //is substring right?
                .objectiveStatus(Optional.ofNullable(descriptionOf(objective.getObjectiveStatus(), OASYSRCMS_OBJ_STATUS_CODE)).map(result -> result.substring(1, 50)).orElse(UNMAPPED))
                .workSummary1(Optional.ofNullable(descriptionOf(objective.getActionCode1(), OASYSRCMS_INTERVENTION)).map(result -> result.substring(1, 50)).orElse(null))
                .workSummary2(Optional.ofNullable(descriptionOf(objective.getActionCode2(), OASYSRCMS_INTERVENTION)).map(result -> result.substring(1, 50)).orElse(null))
                .workSummary3(Optional.ofNullable(descriptionOf(objective.getActionCode3(), OASYSRCMS_INTERVENTION)).map(result -> result.substring(1, 50)).orElse(null))
                .text1(objective.getActionText1().substring(1, 100))
                .text2(objective.getActionText2().substring(1, 250))
                .text3(objective.getActionText3().substring(1, 250))
                .build();
    }

    private String descriptionOf(String sourceVal, Long codeType) {
        //TODO record any mapping failures
        Optional<MappingCodeData> maybeMapped = Optional.ofNullable(sourceVal).flatMap(
                sv -> Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                        .codeType(codeType)
                        .sourceValue(sourceVal)
                        .build())));

        return maybeMapped.map(MappingCodeData::getDescription).orElse(null);

    }

    private String targetValueOf(String sourceVal, Long codeType) {
        //TODO record any mapping failures
        Optional<MappingCodeData> maybeMapped = Optional.ofNullable(sourceVal).flatMap(
                sv -> Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                        .codeType(codeType)
                        .sourceValue(sourceVal)
                        .build())));

        return maybeMapped.map(MappingCodeData::getTargetValue).orElse(null);

    }

    private OasysAssessmentSummary deliusOasysAssessmentSummaryOf(Assessment ndhAssessment) {

        return OasysAssessmentSummary.builder()
                .caseReferenceNumber(ndhAssessment.getCmsProbNumber())
                .eventNumber(ndhAssessment.getEventNumber())
                .dateAssessmentCompleted(ndhAssessment.getDateAssessmentCompleted())
                .oasysSection3Score(ndhAssessment.getSection3CrimScore())
                .oasysSection4Score(ndhAssessment.getSection4CrimScore())
                .oasysSection6Score(ndhAssessment.getSection6CrimScore())
                .oasysSection7Score(ndhAssessment.getSection7CrimScore())
                .oasysSection8Score(ndhAssessment.getSection8CrimScore())
                .oasysSection9Score(ndhAssessment.getSection9CrimScore())
                .oasysSection11Score(ndhAssessment.getSection11CrimScore())
                .oasysSection12Score(ndhAssessment.getSection12CrimScore())
                .concernFlags(deliusConcernFlagsOf(ndhAssessment.getConcernFlags()))
                .oasysId(ndhAssessment.getAssessmentGUID())
                .oasysTotalScore(ndhAssessment.getTotalScore())
                .purposeOfAssessmentCode(ndhAssessment.getPurposeOfAssessmentCode())
                .purposeOfAssessmentDescription(ndhAssessment.getPurposeOfAssessmentDescription().substring(1, 50))
                .dateCreated(ndhAssessment.getDateCreated())
                .assessedBy(ndhAssessment.getAssessedBy())
                .court(ndhAssessment.getCourtCode())
                .courtType(Optional.ofNullable(targetValueOf(ndhAssessment.getCourtType(), OASYSRPCMS_COURTTYPE)).orElse(null))
                .offenceCode(ndhAssessment.getOffence().getOffenceGroupCode().concat(ndhAssessment.getOffence().getOffenceSubCode()))
                .ogrsScore1(ndhAssessment.getOgrsScore1())
                .ogrsScore2(ndhAssessment.getOgrsScore2())
                .ogpNotCalculated(ndhAssessment.getOgpNotCalculated())
                .ovpNotCalculated(ndhAssessment.getOvpNotCalculated())
                .ogpScore1(ndhAssessment.getOgpScore1())
                .ogpScore2(ndhAssessment.getOgpScore2())
                .ovpScore1(ndhAssessment.getOvpScore1())
                .ovpScore2(ndhAssessment.getOvpScore2())
                .ogrsRiskRecon(ndhAssessment.getOgrsRiskRecon())
                .ogpRiskRecon(ndhAssessment.getOgpRiskRecon())
                .ovpRiskRecon(ndhAssessment.getOvpRiskRecon())
                .layer1Obj(deliusLayersOf(ndhAssessment.getLayer1Obj()))
                .sentencePlanReviewDate(ndhAssessment.getSentencePlanReviewDate())
                .sentencePlanInitialDate(ndhAssessment.getSentencePlanInitialDate())
                .reviewTerminated(ndhAssessment.getReviewTerminated())
                .reviewNumber(ndhAssessment.getReviewNumber())
                .layerType(ndhAssessment.getLayerType())
                .build();
    }

    private String deliusLayersOf(String layer1Obj) {
        return Optional.ofNullable(layer1Obj)
                .map(flags -> Arrays
                        .stream(flags.split(","))
                        .map(part -> deliusLayerOf.apply(part))
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    private String deliusRiskFlagsOf(String riskFlags) {
        return Optional.ofNullable(riskFlags)
                .map(flags -> Arrays
                        .stream(flags.split(","))
                        .map(part -> deliusRiskFlagOf.apply(part))
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    private String deliusConcernFlagsOf(String concernFlags) {
        return Optional.ofNullable(concernFlags)
                .map(flags -> Arrays
                        .stream(flags.split(","))
                        .map(part -> deliusConcernFlagOf.apply(part))
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public MappingRepository getMappingRepository() {
        return mappingRepository;
    }
}
