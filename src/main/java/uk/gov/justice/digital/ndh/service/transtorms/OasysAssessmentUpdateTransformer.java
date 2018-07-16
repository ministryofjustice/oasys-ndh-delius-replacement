package uk.gov.justice.digital.ndh.service.transtorms;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.*;
import uk.gov.justice.digital.ndh.api.oasys.CmsUpdate;

@Component
public class OasysAssessmentUpdateTransformer {

    public SoapEnvelope deliusAssessmentUpdateOF (CmsUpdate cmsUpdate){
        OasysAssessmentSummary.builder()
                .caseReferenceNumber(cmsUpdate.getAssessment().getCmsProbNumber())
                .eventNumber(cmsUpdate.getAssessment().getEventNumber())
                .dateAssessmentCompleted(cmsUpdate.getAssessment().getDateAssessmentCompleted())
                .oasysSection3Score(cmsUpdate.getAssessment().getSection3CrimScore())
                .oasysSection4Score(cmsUpdate.getAssessment().getSection4CrimScore())
                .oasysSection6Score(cmsUpdate.getAssessment().getSection6CrimScore())
                .oasysSection7Score(cmsUpdate.getAssessment().getSection7CrimScore())
                .oasysSection8Score(cmsUpdate.getAssessment().getSection8CrimScore())
                .oasysSection9Score(cmsUpdate.getAssessment().getSection9CrimScore())
                .oasysSection11Score(cmsUpdate.getAssessment().getSection11CrimScore())
                .oasysSection12Score(cmsUpdate.getAssessment().getSection12CrimScore())
                //.concernFlags()
                .oasysId(cmsUpdate.getAssessment().getAssessmentGUID())
                .oasysTotalScore(cmsUpdate.getAssessment().getTotalScore())
                .purposeOfAssessmentCode(cmsUpdate.getAssessment().getPurposeOfAssessmentCode())
                .purposeOfAssessmentDescription(cmsUpdate.getAssessment().getPurposeOfAssessmentDescription().substring(1,50))
                .dateCreated(cmsUpdate.getAssessment().getDateCreated())
                .assessedBy(cmsUpdate.getAssessment().getAssessedBy())
                .court(cmsUpdate.getAssessment().getCourtCode())
                //.courtType()
                .offenceCode(cmsUpdate.getAssessment().getOffence().getOffenceGroupCode().concat(cmsUpdate.getAssessment().getOffence().getOffenceSubCode()))
                //.ogrsScore1()
                //.ogrsScore2()
                .ogpNotCalculated(cmsUpdate.getAssessment().getOgpNotCalculated())
                .ovpNotCalculated(cmsUpdate.getAssessment().getOvpNotCalculated())
                .ogpScore1(cmsUpdate.getAssessment().getOgpScore1())
                .ogpScore2(cmsUpdate.getAssessment().getOgpScore2())
                .ovpScore1(cmsUpdate.getAssessment().getOvpScore1())
                .ovpScore2(cmsUpdate.getAssessment().getOvpScore2())
                .ogrsRiskRecon(cmsUpdate.getAssessment().getOgrsRiskRecon())
                .ogpRiskRecon(cmsUpdate.getAssessment().getOgpRiskRecon())
                .ovpRiskRecon(cmsUpdate.getAssessment().getOvpRiskRecon())
                //.layer1Obj()
                .sentencePlanReviewDate(cmsUpdate.getAssessment().getSentencePlanReviewDate())
                .sentencePlanInitialDate(cmsUpdate.getAssessment().getSentencePlanInitialDate())
                .reviewTerminated(cmsUpdate.getAssessment().getReviewTerminated())
                //.reviewNumber(cmsUpdate.getAssessment()
                .layerType(cmsUpdate.getAssessment().getLayerType())
                .build();


        OasysSupervisionPlan.builder()
                .caseReferenceNumber(cmsUpdate.getAssessment().)







    }

}
