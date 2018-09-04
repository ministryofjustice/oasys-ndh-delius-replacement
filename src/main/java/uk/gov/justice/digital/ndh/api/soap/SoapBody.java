package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.delius.response.GetSubSetOffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;
import uk.gov.justice.digital.ndh.api.oasys.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.OffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.InitialSearchResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.OffenderDetailsResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;

@Value
@EqualsAndHashCode(exclude = "fault") //Compare "null" nodes returns false, so exclude
@Builder
public class SoapBody {
    @JsonProperty("RiskUpdateRequest")
    private SubmitRiskDataRequest riskUpdateRequest;

    @JacksonXmlProperty(localName = "RiskUpdateResponse", namespace = "http://www.hp.com/NDH_Web_Service/riskupdateresponse")
    @JsonProperty("RiskUpdateResponse")
    private RiskUpdateResponse riskUpdateResponse;

    @JsonProperty("CMSUpdate")
    private CmsUpdate cmsUpdate;

    @JsonProperty("InitialSearchRequest")
    private InitialSearchRequest initialSearchRequest;

    @JacksonXmlProperty(localName = "InitialSearchResponse", namespace = "http://www.hp.com/NDH_Web_Service/initialsearchresponse")
    @JsonProperty("InitialSearchResponse")
    private InitialSearchResponse initialSearchResponse;

    @JsonProperty("OffenderDetailsRequest")
    private OffenderDetailsRequest offenderDetailsRequest;

    @JacksonXmlProperty(localName = "OffenderDetailsResponse", namespace = "http://www.hp.com/NDH_Web_Service/Offender_Details_Response")
    @JsonProperty("OffenderDetailsResponse")
    private OffenderDetailsResponse offenderDetailsResponse;

    @JacksonXmlProperty(localName = "SubmitAssessmentSummaryRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private SubmitAssessmentSummaryRequest submitAssessmentSummaryRequest;

    @JacksonXmlProperty(localName = "SubmitRiskDataRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest submitRiskDataRequest;

    @JacksonXmlProperty(localName = "GetSubSetOffenderEventRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private GetSubSetOffenderEventRequest getSubSetOffenderEventRequest;

    @JsonProperty("GetSubSetOffenderDetailsResponse")
    private GetSubSetOffenderDetailsResponse getSubSetOffenderDetailsResponse;

    @JsonProperty(value = "Fault")
    private JsonNode fault;

    @JsonIgnore
    public boolean isSoapFault() {
        return fault != null && !fault.isNull();
    }
}
