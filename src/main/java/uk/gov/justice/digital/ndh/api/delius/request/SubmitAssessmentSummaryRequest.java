package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SubmitAssessmentSummaryRequest {
    @JacksonXmlProperty(localName = "OASYSAssessmentSummary", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private OasysAssessmentSummary oasysAssessmentSummary;
    @JacksonXmlProperty(localName = "OASYSSupervisionPlan", namespace = "http://www.bconline.co.uk/oasys/assessment")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<OasysSupervisionPlan> oasysSupervisionPlans;
    @JacksonXmlProperty(localName = "RiskType", namespace = "http://www.bconline.co.uk/oasys/risk")
    private RiskType riskType;
}
