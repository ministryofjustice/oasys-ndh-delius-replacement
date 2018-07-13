package uk.gov.justice.digital.ndh.api.delius;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitAssessmentSummaryRequest {
    @JacksonXmlProperty(localName = "OASYSAssessmentSummary", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private OasysAssessmentSummary oasysAssessmentSummary;
    @JacksonXmlProperty(localName = "OASYSSupervisionPlan", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private OasysSupervisionPlan oasysSupervisionPlan;
    @JacksonXmlProperty(localName = "RiskType", namespace = "http://www.bconline.co.uk/oasys/risk")
    private RiskType riskType;
}
