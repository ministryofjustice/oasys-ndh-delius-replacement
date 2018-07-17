package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Assessment {

//            <xs:element name="CMSProbNumber" type="dt:CMSNumber"/>
    @JsonProperty("CMSProbNumber")
    private String cmsProbNumber;
//            <xs:element name="EventNumber" type="xs:integer">
    @JsonProperty("EventNumber")
    private String eventNumber;
//            <xs:element name="ICMSReference" type="dt:ICMSReference"/>
    @JsonProperty("ICMSReference")
    private String icmsReference;
//            <xs:element name="OffenderGUID" type="xs:integer">
    @JsonProperty("OffenderGUID")
    private String offenderGuid;
//            <xs:element name="DateAssessmentCompleted" type="xs:date" minOccurs="0"/>
    @JsonProperty("DateAssessmentCompleted")
    private String dateAssessmentCompleted;
//            <xs:element name="DeletedIndicator" type="xs:integer" minOccurs="0">
    @JsonProperty("DeletedIndicator")
    private String deletedIndicator;
//            <xs:element name="AssessmentGUID" type="xs:integer">
    @JsonProperty("AssessmentGUID")
    private String assessmentGUID;
//            <xs:element name="RiskFlags" minOccurs="0">
    @JsonProperty("RiskFlags")
    private String riskFlags;
//            <xs:element name="ConcernFlags" minOccurs="0">
    @JsonProperty("ConcernFlags")
    private String concernFlags;
//            <xs:element name="TotalScore" minOccurs="0">
    @JsonProperty("TotalScore")
    private String totalScore;
//            <xs:element name="PurposeOfAssessmentCode" type="dt:ElementCode" minOccurs="0"/>
    @JsonProperty("PurposeOfAssessmentCode")
    private String purposeOfAssessmentCode;
//            <xs:element name="PurposeOfAssessmentDescription" minOccurs="0">
    @JsonProperty("PurposeOfAssessmentDescription")
    private String purposeOfAssessmentDescription;
//            <xs:element name="DateCreated" type="xs:date"/>
    @JsonProperty("DateCreated")
    private String dateCreated;
//            <xs:element name="AssessedBy" type="dt:PersonName" minOccurs="0"/>
    @JsonProperty("AssessedBy")
    private String assessedBy;
//            <xs:element name="CourtCode" type="dt:SharedKeyCode" minOccurs="0"/>
    @JsonProperty("CourtCode")
    private String courtCode;
//            <xs:element name="CourtType" type="dt:CourtType" minOccurs="0"/>
    @JsonProperty("CourtType")
    private String courtType;
//            <xs:element name="CourtName" type="dt:Text100" minOccurs="0"/>
    @JsonProperty("CourtName")
    private String courtName;
//            <xs:element name="Offence" type="dt:OffenceDetail" minOccurs="0"/>
    @JsonProperty("Offence")
    private Offence offence;
//            <xs:element name="OGRSScore1" type="xs:decimal" minOccurs="0"/>
    @JsonProperty("OGRSScore1")
    private String ogrsScore1;
//            <xs:element name="OGRSScore2" type="xs:decimal" minOccurs="0"/>
    @JsonProperty("OGRSScore2")
    private String ogrsScore2;
//            <xs:element name="SentencePlanInitialDate" type="xs:date" minOccurs="0"/>
    @JsonProperty("SentencePlanInitialDate")
    private String sentencePlanInitialDate;
//            <xs:element name="SentencePlanReviewDate" type="xs:date" minOccurs="0"/>
    @JsonProperty("SentencePlanReviewDate")
    private String sentencePlanReviewDate;
//            <xs:element name="ReviewTerminated" minOccurs="0">
    @JsonProperty("ReviewTerminated")
    private String reviewTerminated;
//            <xs:element name="ReviewNumber" minOccurs="0">
    @JsonProperty("ReviewNumber")
    private String reviewNumber;
//            <xs:element name="OGPNotCalculated" type="xs:integer" minOccurs="0"/>
    @JsonProperty("OGPNotCalculated")
    private String ogpNotCalculated;
//            <xs:element name="OVPNotCalculated" type="xs:integer" minOccurs="0"/>
    @JsonProperty("OVPNotCalculated")
    private String ovpNotCalculated;
//            <xs:element name="OGPScore1" type="xs:integer" minOccurs="0"/>
    @JsonProperty("OGPScore1")
    private String ogpScore1;
//            <xs:element name="OGPScore2" type="xs:integer" minOccurs="0"/>
    @JsonProperty("OGPScore2")
    private String ogpScore2;
//            <xs:element name="OVPScore1" type="xs:integer" minOccurs="0"/>
    @JsonProperty("OVPScore1")
    private String ovpScore1;
//            <xs:element name="OVPScore2" type="xs:integer" minOccurs="0"/>
    @JsonProperty("OVPScore2")
    private String ovpScore2;
//            <xs:element name="OGRSRiskRecon" type="dt:ElementCode" minOccurs="0">
    @JsonProperty("OGRSRiskRecon")
    private String ogrsRiskRecon;
//            <xs:element name="OGPRiskRecon" type="dt:ElementCode" minOccurs="0"/>
    @JsonProperty("OGPRiskRecon")
    private String ogpRiskRecon;
//            <xs:element name="OVPRiskRecon" type="dt:ElementCode" minOccurs="0"/>
    @JsonProperty("OVPRiskRecon")
    private String ovpRiskRecon;
//            <xs:element name="LayerType" type="dt:ElementCode"/>
    @JsonProperty("LayerType")
    private String layerType;
//            <xs:element name="Section3CrimScore" minOccurs="0">
    @JsonProperty("Section3CrimScore")
    private String section3CrimScore;
//            <xs:element name="Section4CrimScore" minOccurs="0">
    @JsonProperty("Section4CrimScore")
    private String section4CrimScore;
//            <xs:element name="Section6CrimScore" minOccurs="0">
    @JsonProperty("Section6CrimScore")
    private String section6CrimScore;
//            <xs:element name="Section7CrimScore" type="xs:string" minOccurs="0">
    @JsonProperty("Section7CrimScore")
    private String section7CrimScore;
//            <xs:element name="Section8CrimScore" minOccurs="0">
    @JsonProperty("Section8CrimScore")
    private String section8CrimScore;
//            <xs:element name="Section9CrimScore" minOccurs="0">
    @JsonProperty("Section9CrimScore")
    private String section9CrimScore;
//            <xs:element name="Section11CrimScore" minOccurs="0">
    @JsonProperty("Section11CrimScore")
    private String section11CrimScore;
//            <xs:element name="Section12CrimScore" minOccurs="0">
    @JsonProperty("Section12CrimScore")
    private String section12CrimScore;
//            <xs:element name="LAOIndicator" type="xs:string" minOccurs="0">
    @JsonProperty("LAOIndicator")
    private String laoIndicator;
//            <xs:element name="TierCode" minOccurs="0">
    @JsonProperty("TierCode")
    private String tierCode;
//            <xs:element name="Layer1Obj" type="xs:string" minOccurs="0">
    @JsonProperty("Layer1Obj")
    private String layer1Obj;
//            <xs:element name="Risk" type="dt:Risk" minOccurs="0"/>
    @JsonProperty("Risk")
    private Risk risk;
}
