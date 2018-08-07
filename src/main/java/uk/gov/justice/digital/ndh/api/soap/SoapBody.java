package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.delius.request.InitialSearchRequest;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.InitialSearchResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;

@Value
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

    @JacksonXmlProperty(localName = "SubmitAssessmentSummaryRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private SubmitAssessmentSummaryRequest submitAssessmentSummaryRequest;

    @JacksonXmlProperty(localName = "SubmitRiskDataRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest submitRiskDataRequest;


}
