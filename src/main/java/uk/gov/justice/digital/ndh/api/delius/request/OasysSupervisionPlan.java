package uk.gov.justice.digital.ndh.api.delius.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OasysSupervisionPlan {
    @JacksonXmlProperty(localName = "CaseReferenceNumber", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String caseReferenceNumber;
    @JacksonXmlProperty(localName = "OASYS_ID", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String oasysId;
    @JacksonXmlProperty(localName = "ObjectiveNumber", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String objectiveNumber;
    @JacksonXmlProperty(localName = "Need1", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String need1;
    @JacksonXmlProperty(localName = "Need2", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String need2;
    @JacksonXmlProperty(localName = "Need3", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String need3;
    @JacksonXmlProperty(localName = "Need4", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String need4;
    @JacksonXmlProperty(localName = "Objective", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String objective;
    @JacksonXmlProperty(localName = "ObjectiveStatus", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String objectiveStatus;
    @JacksonXmlProperty(localName = "WorkSummary1", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String workSummary1;
    @JacksonXmlProperty(localName = "WorkSummary2", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String workSummary2;
    @JacksonXmlProperty(localName = "WorkSummary3", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String workSummary3;
    @JacksonXmlProperty(localName = "Text1", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String text1;
    @JacksonXmlProperty(localName = "Text2", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String text2;
    @JacksonXmlProperty(localName = "Text3", namespace = "http://www.bconline.co.uk/oasys/assessment")
    private String text3;
}
