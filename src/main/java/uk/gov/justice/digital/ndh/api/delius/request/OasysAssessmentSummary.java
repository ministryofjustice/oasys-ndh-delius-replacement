package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OasysAssessmentSummary {
    @JacksonXmlProperty(localName = "CaseReferenceNumber", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String caseReferenceNumber;
    @JacksonXmlProperty(localName = "EventNumber", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String eventNumber;
    @JacksonXmlProperty(localName = "DateAssessmentCompleted", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String dateAssessmentCompleted;
    @JacksonXmlProperty(localName = "OASYSSection2Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection2Scores;
    @JacksonXmlProperty(localName = "OASYSSection3Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection3Scores;
    @JacksonXmlProperty(localName = "OASYSSection4Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection4Scores;
    @JacksonXmlProperty(localName = "OASYSSection5Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection5Scores;
    @JacksonXmlProperty(localName = "OASysSection6Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection6Scores;
    @JacksonXmlProperty(localName = "OASYSSection7Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection7Scores;
    @JacksonXmlProperty(localName = "OASYSSection8Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection8Scores;
    @JacksonXmlProperty(localName = "OASYSSection9Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection9Scores;
    @JacksonXmlProperty(localName = "OASYSSection10Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection10Scores;
    @JacksonXmlProperty(localName = "OASYSSection11Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection11Scores;
    @JacksonXmlProperty(localName = "OASYSSection12Scores", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection12Scores;
    @JacksonXmlProperty(localName = "OASYSSection3Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection3Score;
    @JacksonXmlProperty(localName = "OASYSSection4Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection4Score;
    @JacksonXmlProperty(localName = "OASYSSection6Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection6Score;
    @JacksonXmlProperty(localName = "OASYSSection7Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection7Score;
    @JacksonXmlProperty(localName = "OASYSSection8Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection8Score;
    @JacksonXmlProperty(localName = "OASYSSection9Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection9Score;
    @JacksonXmlProperty(localName = "OASYSSection11Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection11Score;
    @JacksonXmlProperty(localName = "OASYSSection12Score", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysSection12Score;
    @JacksonXmlProperty(localName = "ConcernFlags", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String concernFlags;
    @JacksonXmlProperty(localName = "OASYS_ID", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysId;
    @JacksonXmlProperty(localName = "OASYSTotalScore", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysTotalScore;
    @JacksonXmlProperty(localName = "PurposeOfAssessmentCode", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String purposeOfAssessmentCode;
    @JacksonXmlProperty(localName = "PurposeOfAssessmentDescription", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String purposeOfAssessmentDescription;
    @JacksonXmlProperty(localName = "DateCreated", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String dateCreated;
    @JacksonXmlProperty(localName = "AssessedBy", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String assessedBy;
    @JacksonXmlProperty(localName = "Court", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String court;
    @JacksonXmlProperty(localName = "CourtType", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String courtType;
    @JacksonXmlProperty(localName = "OffenceCode", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String offenceCode;
    @JacksonXmlProperty(localName = "OGRSScore1", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogrsScore1;
    @JacksonXmlProperty(localName = "OGRSScore2", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogrsScore2;
    @JacksonXmlProperty(localName = "OGPNotCalculated", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogpNotCalculated;
    @JacksonXmlProperty(localName = "OVPNotCalculated", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ovpNotCalculated;
    @JacksonXmlProperty(localName = "OGPScore1", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogpScore1;
    @JacksonXmlProperty(localName = "OGPScore2", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogpScore2;
    @JacksonXmlProperty(localName = "OVPScore1", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ovpScore1;
    @JacksonXmlProperty(localName = "OVPScore2", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ovpScore2;
    @JacksonXmlProperty(localName = "OGRSRiskRecon", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogrsRiskRecon;
    @JacksonXmlProperty(localName = "OGPRiskRecon", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ogpRiskRecon;
    @JacksonXmlProperty(localName = "OVPRiskRecon", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String ovpRiskRecon;
    @JacksonXmlProperty(localName = "TierCode", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String tierCode;
    @JacksonXmlProperty(localName = "Layer1Obj", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String layer1Obj;
    @JacksonXmlProperty(localName = "SentencePlanReviewDate", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String sentencePlanReviewDate;
    @JacksonXmlProperty(localName = "SentencePlanInitialDate", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String sentencePlanInitialDate;
    @JacksonXmlProperty(localName = "ReviewTerminated", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String reviewTerminated;
    @JacksonXmlProperty(localName = "ReviewNumber", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String reviewNumber;
    @JacksonXmlProperty(localName = "LayerType", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String layerType;

}
