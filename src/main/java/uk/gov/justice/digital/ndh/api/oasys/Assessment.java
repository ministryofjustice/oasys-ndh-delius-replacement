package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@Builder
public class Assessment {

    /*
            <ass:CMSProbNumber>H923505</ass:CMSProbNumber>
        <ass:EventNumber>1</ass:EventNumber>
        <ass:OffenderGUID>7294806</ass:OffenderGUID>
        <ass:DateAssessmentCompleted>2018-06-19</ass:DateAssessmentCompleted>
        <ass:DeletedIndicator>0</ass:DeletedIndicator>
        <ass:AssessmentGUID>9485345</ass:AssessmentGUID>
        <ass:RiskFlags>,,,,,,,,</ass:RiskFlags>
        <ass:ConcernFlags>,,,,,,,</ass:ConcernFlags>
        <ass:TotalScore>90</ass:TotalScore>
        <ass:PurposeOfAssessmentCode>430</ass:PurposeOfAssessmentCode>
        <ass:PurposeOfAssessmentDescription>CJA2003 - Start of Community Order</ass:PurposeOfAssessmentDescription>
        <ass:DateCreated>2018-06-19</ass:DateCreated>
        <ass:AssessedBy>Testing Admin Testing National</ass:AssessedBy>
        <ass:CourtCode>STAFCC</ass:CourtCode>
        <ass:CourtType>CC</ass:CourtType>
        <ass:CourtName>Stafford Crown Court</ass:CourtName>
        <ass:Offence>
            <dom:OffenceGroupCode>104</dom:OffenceGroupCode>
            <dom:OffenceSubCode>23</dom:OffenceSubCode>
            <dom:AdditionalIndicator>N</dom:AdditionalIndicator>
        </ass:Offence>
        <ass:OGRSScore1>65</ass:OGRSScore1>
        <ass:OGRSScore2>80</ass:OGRSScore2>
        <ass:SentencePlanInitialDate>2018-06-19</ass:SentencePlanInitialDate>
        <ass:ReviewTerminated>N</ass:ReviewTerminated>
        <ass:OGPNotCalculated>0</ass:OGPNotCalculated>
        <ass:OVPNotCalculated>0</ass:OVPNotCalculated>
        <ass:OGPScore1>50</ass:OGPScore1>
        <ass:OGPScore2>65</ass:OGPScore2>
        <ass:OVPScore1>9</ass:OVPScore1>
        <ass:OVPScore2>15</ass:OVPScore2>
        <ass:OGRSRiskRecon>H</ass:OGRSRiskRecon>
        <ass:OGPRiskRecon>M</ass:OGPRiskRecon>
        <ass:OVPRiskRecon>L</ass:OVPRiskRecon>
        <ass:LayerType>LAYER_3</ass:LayerType>
        <ass:Section3CrimScore>3</ass:Section3CrimScore>
        <ass:Section4CrimScore>2</ass:Section4CrimScore>
        <ass:Section6CrimScore>1</ass:Section6CrimScore>
        <ass:Section7CrimScore>1</ass:Section7CrimScore>
        <ass:Section8CrimScore>3</ass:Section8CrimScore>
        <ass:Section9CrimScore>4</ass:Section9CrimScore>
        <ass:Section11CrimScore>1</ass:Section11CrimScore>
        <ass:Section12CrimScore>0</ass:Section12CrimScore>
        <ass:LAOIndicator>N</ass:LAOIndicator>

     */
    @JsonProperty("CMSProbNumber")
    private String cmsProbNumber;
    @JsonProperty("EventNumber")
    private String eventNumber;
    @JsonProperty("OffenderGUID")
    private String offenderGuid;
    @JsonProperty("DateAssessmentCompleted")
    private Date dateAssessmentCompleted;
    @JsonProperty("DeletedIndicator")
    private Long deletedIndicator;
    @JsonProperty("AssessmentGUID")
    private Long assessmentGUID;
    @JsonProperty("RiskFlags")
    private String riskFlags;
    @JsonProperty("ConcernFlags")
    private String concernFlags;
    @JsonProperty("TotalScore")
    private Long totalScore;
    @JsonProperty("PurposeOfAssessmentCode")
    private Long purposeOfAssessmentCode;
    @JsonProperty("PurposeOfAssessmentDescription")
    private String purposeOfAssessmentDescription;
    @JsonProperty("DateCreated")
    private Date dateCreated;
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
    private Long ogrsScore1;
    @JsonProperty("OGRSScore2")
    private Long ogrsScore2;
    @JsonProperty("SentencePlanInitialDate")
    private Date sentencePlanInitialDate;
    @JsonProperty("ReviewTerminated")
    private String reviewTerminated;
    @JsonProperty("OGPNotCalculated")
    private Long ogpNotCalculated;
    @JsonProperty("OVPNotCalculated")
    private Long ovpNotCalculated;
    @JsonProperty("OGPScore1")
    private Long ogpScore1;
    @JsonProperty("OGPScore2")
    private Long ogpScore2;
    @JsonProperty("OVPScore1")
    private Long ovpScore1;
    @JsonProperty("OVPScore2")
    private Long ovpScore2;
    @JsonProperty("OGRSRiskRecon")
    private String ogrsRiskRecon;
    @JsonProperty("OGPRiskRecon")
    private String ogpRiskRecon;
    @JsonProperty("OVPRiskRecon")
    private String ovpRiskRecon;
    @JsonProperty("LayerType")
    private String layerType;
    @JsonProperty("Section3CrimScore")
    private Long section3CrimScore;
    @JsonProperty("Section4CrimScore")
    private Long section4CrimScore;
    @JsonProperty("Section6CrimScore")
    private Long section6CrimScore;
    @JsonProperty("Section7CrimScore")
    private Long section7CrimScore;
    @JsonProperty("Section8CrimScore")
    private Long section8CrimScore;
    @JsonProperty("Section9CrimScore")
    private Long section9CrimScore;
    @JsonProperty("Section11CrimScore")
    private Long section11CrimScore;
    @JsonProperty("Section12CrimScore")
    private Long section12CrimScore;
    @JsonProperty("LAOIndicator")
    private String laoIndicator;

}
