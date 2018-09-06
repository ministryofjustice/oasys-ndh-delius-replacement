package uk.gov.justice.digital.ndh.service.transforms;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.OasysAssessmentSummary;
import uk.gov.justice.digital.ndh.api.delius.request.OasysSupervisionPlan;
import uk.gov.justice.digital.ndh.api.delius.request.RiskType;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.Assessment;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;
import uk.gov.justice.digital.ndh.api.oasys.request.Objective;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;

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
    private final MappingService mappingService;
    private final CommonTransformer commonTransformer;

    private final Function<String, String> deliusLayerOf;
    private final Function<String, String> deliusRiskFlagOf;
    private final Function<String, String> deliusConcernFlagOf;

    @Autowired
    public OasysAssessmentUpdateTransformer(MappingService mappingService, CommonTransformer commonTransformer) {
        this.mappingService = mappingService;
        this.commonTransformer = commonTransformer;

        deliusLayerOf = part -> "".equals(part) ? "" : mappingService.targetValueOf(part, OASYSRPCMS_LAYER1OBJ);
        deliusRiskFlagOf = part -> "".equals(part) ? "L" : part;
        deliusConcernFlagOf = part -> {
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
    }

    public SoapEnvelope deliusAssessmentUpdateOf(SoapEnvelope ndhSoapEnvelope) {

        final String correlationID = ndhSoapEnvelope.getBody().getCmsUpdate().getHeader().getCorrelationID();

        return SoapEnvelope.builder()
                .header(commonTransformer.deliusSoapHeaderOf(correlationID))
                .body(SoapBody
                        .builder()
                        .submitAssessmentSummaryRequest(SubmitAssessmentSummaryRequest
                                .builder()
                                //TODO: May have to guard against NPEs for the following. Will every update have an Assessment??
                                .oasysAssessmentSummary(deliusOasysAssessmentSummaryOf(ndhSoapEnvelope.getBody().getCmsUpdate().getAssessment()))
                                .oasysSupervisionPlans(deliusSupervisionPlansOf(ndhSoapEnvelope.getBody().getCmsUpdate()))
                                .riskType(deliusRiskOf(ndhSoapEnvelope.getBody().getCmsUpdate().getAssessment()))
                                .build())
                        .build())
                .build();
    }

    private RiskType deliusRiskOf(Assessment assessment) {
        return Optional.ofNullable(assessment).map(
                a -> RiskType.builder()
                        .caseReferenceNumber(assessment.getCmsProbNumber())
                        .riskOfHarm(deliusRiskFlagsOf(a.getRiskFlags()))
                        .build()).orElse(null);

    }

    private List<OasysSupervisionPlan> deliusSupervisionPlansOf(CmsUpdate cmsUpdate) {
        return Optional.ofNullable(cmsUpdate.getObjectives())
                .map(objectives -> objectives.stream()
                        .map(objective -> deliusSupervisionPlanOf(objective, cmsUpdate.getAssessment()))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private OasysSupervisionPlan deliusSupervisionPlanOf(Objective objective, Assessment assessment) throws NDHMappingException {
        return OasysSupervisionPlan.builder()
                .caseReferenceNumber(assessment.getCmsProbNumber())
                .oasysId(assessment.getAssessmentGUID())
                .objectiveNumber(objective.getObjectiveNumber())
                .need1(Optional.ofNullable(mappingService.descriptionOf(objective.getNeed1(), OASYS_CRMS_CRIM_NEED)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .need2(Optional.ofNullable(mappingService.descriptionOf(objective.getNeed2(), OASYS_CRMS_CRIM_NEED)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .need3(Optional.ofNullable(mappingService.descriptionOf(objective.getNeed3(), OASYS_CRMS_CRIM_NEED)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .need4(Optional.ofNullable(mappingService.descriptionOf(objective.getNeed4(), OASYS_CRMS_CRIM_NEED)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .objective(Optional.ofNullable(objective.getObjectiveDescription()).map(o -> commonTransformer.limitLength(o, 50)).orElse(null))
                .objectiveStatus(Optional.ofNullable(mappingService.descriptionOf(objective.getObjectiveStatus(), OASYSRCMS_OBJ_STATUS_CODE)).map(result -> commonTransformer.limitLength(result, 50)).orElse(UNMAPPED))
                .workSummary1(Optional.ofNullable(mappingService.descriptionOf(objective.getActionCode1(), OASYSRCMS_INTERVENTION)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .workSummary2(Optional.ofNullable(mappingService.descriptionOf(objective.getActionCode2(), OASYSRCMS_INTERVENTION)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .workSummary3(Optional.ofNullable(mappingService.descriptionOf(objective.getActionCode3(), OASYSRCMS_INTERVENTION)).map(result -> commonTransformer.limitLength(result, 50)).orElse(null))
                .text1(Optional.ofNullable(objective.getActionText1()).map(a -> commonTransformer.limitLength(a, 100)).orElse(null))
                .text2(Optional.ofNullable(objective.getActionText2()).map(a -> commonTransformer.limitLength(a, 250)).orElse(null))
                .text3(Optional.ofNullable(objective.getActionText3()).map(a -> commonTransformer.limitLength(a, 250)).orElse(null))
                .build();
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
                .purposeOfAssessmentDescription(commonTransformer.limitLength(ndhAssessment.getPurposeOfAssessmentDescription(), 50))
                .dateCreated(ndhAssessment.getDateCreated())
                .assessedBy(ndhAssessment.getAssessedBy())
                .court(ndhAssessment.getCourtCode())
                .courtType(Optional.ofNullable(mappingService.targetValueOf(ndhAssessment.getCourtType(), OASYSRPCMS_COURTTYPE)).orElse(null))
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
                        .stream(flags.split(",", -1))
                        .map(deliusLayerOf)
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public String deliusRiskFlagsOf(String riskFlags) {
        return Optional.ofNullable(riskFlags)
                .map(flags -> Arrays
                        .stream(flags.split(",", -1))
                        .map(deliusRiskFlagOf)
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public String deliusConcernFlagsOf(String concernFlags) {
        return Optional.ofNullable(concernFlags)
                .map(flags -> Arrays
                        .stream(flags.split(",", -1))
                        .map(deliusConcernFlagOf)
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public String customIdOf(String oasysAssessmentUpdateRequestXml) throws DocumentException {
        return commonTransformer.evaluateXpathText(oasysAssessmentUpdateRequestXml, "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='CMSUpdate']/*[local-name()='Assessment']/*[local-name()='CMSProbNumber']");
    }

    public String correlationIdOf(String oasysAssessmentUpdateRequestXml) throws DocumentException {
        return commonTransformer.evaluateXpathText(oasysAssessmentUpdateRequestXml, "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='CMSUpdate']/*[local-name()='Header']//*[local-name()='CorrelationID']");
    }


}
