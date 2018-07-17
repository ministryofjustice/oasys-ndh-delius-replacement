package uk.gov.justice.digital.ndh.service.transtorms;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.DeliusAssessmentUpdateSoapBody;
import uk.gov.justice.digital.ndh.api.delius.DeliusAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.DeliusAssessmentUpdateSoapHeader;
import uk.gov.justice.digital.ndh.api.delius.OasysAssessmentSummary;
import uk.gov.justice.digital.ndh.api.delius.OasysCommonHeader;
import uk.gov.justice.digital.ndh.api.delius.OasysSupervisionPlan;
import uk.gov.justice.digital.ndh.api.delius.RiskType;
import uk.gov.justice.digital.ndh.api.delius.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.oasys.Assessment;
import uk.gov.justice.digital.ndh.api.oasys.Header;
import uk.gov.justice.digital.ndh.api.oasys.NdhAssessmentUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.oasys.Objective;
import uk.gov.justice.digital.ndh.api.oasys.Risk;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OasysAssessmentUpdateTransformer {

    public DeliusAssessmentUpdateSoapEnvelope deliusAssessmentUpdateOf(NdhAssessmentUpdateSoapEnvelope ndhSoapEnvelope) {

        return DeliusAssessmentUpdateSoapEnvelope.builder()
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
                                .oasysSupervisionPlans(deliusSupervisionPlansOf(ndhSoapEnvelope.getBody().getCmsUpdate().getObjectives()))
                                .riskType(deliusRiskOf(ndhSoapEnvelope.getBody().getCmsUpdate().getAssessment().getRisk()))
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

    private RiskType deliusRiskOf(Risk ndhRisk) {
        return Optional.ofNullable(ndhRisk).map(
                risk -> RiskType.builder()
                        //.riskOfHarm(ndhRisk.getXYZ())
                        // etc
                        .build()).orElse(null);

    }

    private List<OasysSupervisionPlan> deliusSupervisionPlansOf(List<Objective> ndhObjectives) {
        return Optional.ofNullable(ndhObjectives)
                .map(objectives -> objectives.stream()
                        .map(this::deliusSupervisionPlanOf)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private OasysSupervisionPlan deliusSupervisionPlanOf(Objective objective) {
        return OasysSupervisionPlan.builder()
                //TODO:
                //.caseReferenceNumber(objective.get XYZ)
                // .need1(objective.getXYZ) etc etc

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
                //.concernFlags()
                .oasysId(ndhAssessment.getAssessmentGUID())
                .oasysTotalScore(ndhAssessment.getTotalScore())
                .purposeOfAssessmentCode(ndhAssessment.getPurposeOfAssessmentCode())
                .purposeOfAssessmentDescription(ndhAssessment.getPurposeOfAssessmentDescription().substring(1, 50))
                .dateCreated(ndhAssessment.getDateCreated())
                .assessedBy(ndhAssessment.getAssessedBy())
                .court(ndhAssessment.getCourtCode())
                //.courtType()
                .offenceCode(ndhAssessment.getOffence().getOffenceGroupCode().concat(ndhAssessment.getOffence().getOffenceSubCode()))
                //.ogrsScore1()
                //.ogrsScore2()
                .ogpNotCalculated(ndhAssessment.getOgpNotCalculated())
                .ovpNotCalculated(ndhAssessment.getOvpNotCalculated())
                .ogpScore1(ndhAssessment.getOgpScore1())
                .ogpScore2(ndhAssessment.getOgpScore2())
                .ovpScore1(ndhAssessment.getOvpScore1())
                .ovpScore2(ndhAssessment.getOvpScore2())
                .ogrsRiskRecon(ndhAssessment.getOgrsRiskRecon())
                .ogpRiskRecon(ndhAssessment.getOgpRiskRecon())
                .ovpRiskRecon(ndhAssessment.getOvpRiskRecon())
                //.layer1Obj()
                .sentencePlanReviewDate(ndhAssessment.getSentencePlanReviewDate())
                .sentencePlanInitialDate(ndhAssessment.getSentencePlanInitialDate())
                .reviewTerminated(ndhAssessment.getReviewTerminated())
                .reviewNumber(ndhAssessment.getReviewNumber())
                .layerType(ndhAssessment.getLayerType())
                .build();
    }
}
