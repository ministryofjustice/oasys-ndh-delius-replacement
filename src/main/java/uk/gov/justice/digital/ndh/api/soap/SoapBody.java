package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.justice.digital.ndh.api.delius.request.GetOffenderDetailsRequest;
import uk.gov.justice.digital.ndh.api.delius.request.GetSubSetOffenderEventRequest;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusOffenderDetailsResponse;
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
    // From Oasys (risk update)
    @JsonProperty("RiskUpdateRequest")
    private SubmitRiskDataRequest riskUpdateRequest;

    // To Oasys (risk update response)
    @JacksonXmlProperty(localName = "RiskUpdateResponse", namespace = "http://www.hp.com/NDH_Web_Service/riskupdateresponse")
    @JsonProperty("RiskUpdateResponse")
    private RiskUpdateResponse riskUpdateResponse;

    // From Oasys (assessment update)
    @JsonProperty("CMSUpdate")
    private CmsUpdate cmsUpdate;

    // From Oasys (initial search)
    @JsonProperty("InitialSearchRequest")
    private InitialSearchRequest initialSearchRequest;

    // To Oasys (initial search response)
    @JacksonXmlProperty(localName = "InitialSearchResponse", namespace = "http://www.hp.com/NDH_Web_Service/InitialSearchResponse")
    @JsonProperty("InitialSearchResponse")
    private InitialSearchResponse initialSearchResponse;

    // From Oasys (offender details)
    @JsonProperty("OffenderDetailsRequest")
    private OffenderDetailsRequest offenderDetailsRequest;

    // To Oasys (offender details response)
    @JacksonXmlProperty(localName = "OffenderDetailsResponse", namespace = "http://www.hp.com/NDH_Web_Service/Offender_Details_Response")
    @JsonProperty("OffenderDetailsResponse")
    private OffenderDetailsResponse offenderDetailsResponse;

    // To NDelius (assessment summary)
    @JacksonXmlProperty(localName = "SubmitAssessmentSummaryRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private SubmitAssessmentSummaryRequest submitAssessmentSummaryRequest;

    // To NDelius (risk update)
    @JacksonXmlProperty(localName = "SubmitRiskDataRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest submitRiskDataRequest;

    // To NDelius (initial search)
    @JacksonXmlProperty(localName = "GetSubSetOffenderEventRequest", namespace = "http://www.bconline.co.uk/oasys/messages")
    private GetSubSetOffenderEventRequest getSubSetOffenderEventRequest;

    // From NDelius (initial search response)
    @JsonProperty("GetSubSetOffenderDetailsResponse")
    private GetSubSetOffenderDetailsResponse getSubSetOffenderDetailsResponse;

    // To NDelius (offender details)
    @JacksonXmlProperty(namespace = "http://www.bconline.co.uk/oasys/messages", localName = "GetOffenderDetailsRequest")
    private GetOffenderDetailsRequest getOffenderDetailsRequest;

    // From NDelius (offender details response)
    @JsonProperty("DeliusOffenderDetailsResponse")
    private DeliusOffenderDetailsResponse deliusOffenderDetailsResponse;

    @JsonProperty(value = "Fault")
    private JsonNode fault;

    @JsonIgnore
    public boolean isSoapFault() {
        return fault != null && !fault.isNull();
    }
}
