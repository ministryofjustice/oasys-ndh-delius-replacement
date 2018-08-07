package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.common.ICMSReference;

@Value
@Builder
public class Assessment {

    @JsonProperty("CMSProbNumber")
    private String cmsProbNumber;
    @JsonProperty("EventNumber")
    private String eventNumber;
    @JsonProperty("ICMSReference")
    private ICMSReference icmsReference;
    @JsonProperty("OffenderGUID")
    private String offenderGuid;
    @JsonProperty("DateAssessmentCompleted")
    private String dateAssessmentCompleted;
    @JsonProperty("DeletedIndicator")
    private String deletedIndicator;
    @JsonProperty("AssessmentGUID")
    private String assessmentGUID;
    @JsonProperty("RiskFlags")
    private String riskFlags;
    @JsonProperty("ConcernFlags")
    private String concernFlags;
    @JsonProperty("TotalScore")
    private String totalScore;
    @JsonProperty("PurposeOfAssessmentCode")
    private String purposeOfAssessmentCode;
    @JsonProperty("PurposeOfAssessmentDescription")
    private String purposeOfAssessmentDescription;
    @JsonProperty("DateCreated")
    private String dateCreated;
    @JsonProperty("AssessedBy")
    private String assessedBy;
    @JsonProperty("CourtCode")
    private String courtCode;
    @JsonProperty("CourtType")
    private String courtType;
    @JsonProperty("CourtName")
    private String courtName;
    @JsonProperty("Offence")
    private Offence offence;
    @JsonProperty("OGRSScore1")
    private String ogrsScore1;
    @JsonProperty("OGRSScore2")
    private String ogrsScore2;
    @JsonProperty("SentencePlanInitialDate")
    private String sentencePlanInitialDate;
    @JsonProperty("SentencePlanReviewDate")
    private String sentencePlanReviewDate;
    @JsonProperty("ReviewTerminated")
    private String reviewTerminated;
    @JsonProperty("ReviewNumber")
    private String reviewNumber;
    @JsonProperty("OGPNotCalculated")
    private String ogpNotCalculated;
    @JsonProperty("OVPNotCalculated")
    private String ovpNotCalculated;
    @JsonProperty("OGPScore1")
    private String ogpScore1;
    @JsonProperty("OGPScore2")
    private String ogpScore2;
    @JsonProperty("OVPScore1")
    private String ovpScore1;
    @JsonProperty("OVPScore2")
    private String ovpScore2;
    @JsonProperty("OGRSRiskRecon")
    private String ogrsRiskRecon;
    @JsonProperty("OGPRiskRecon")
    private String ogpRiskRecon;
    @JsonProperty("OVPRiskRecon")
    private String ovpRiskRecon;
    @JsonProperty("LayerType")
    private String layerType;
    @JsonProperty("Section3CrimScore")
    private String section3CrimScore;
    @JsonProperty("Section4CrimScore")
    private String section4CrimScore;
    @JsonProperty("Section6CrimScore")
    private String section6CrimScore;
    @JsonProperty("Section7CrimScore")
    private String section7CrimScore;
    @JsonProperty("Section8CrimScore")
    private String section8CrimScore;
    @JsonProperty("Section9CrimScore")
    private String section9CrimScore;
    @JsonProperty("Section11CrimScore")
    private String section11CrimScore;
    @JsonProperty("Section12CrimScore")
    private String section12CrimScore;
    @JsonProperty("LAOIndicator")
    private String laoIndicator;
    @JsonProperty("TierCode")
    private String tierCode;
    @JsonProperty("Layer1Obj")
    private String layer1Obj;
    @JsonProperty("Risk")
    private Risk risk;
}
