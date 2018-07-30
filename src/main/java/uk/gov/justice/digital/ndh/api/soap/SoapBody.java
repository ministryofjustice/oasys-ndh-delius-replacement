package uk.gov.justice.digital.ndh.api.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.SubmitRiskDataResponse;

@Data
@Builder
public class SoapBody {
    @JsonProperty("RiskUpdateRequest")
    private SubmitRiskDataRequest riskUpdateRequest;

    @JsonProperty("RiskUpdateResponse")
    private SubmitRiskDataResponse riskUpdateResponse;

    @JsonProperty("CMSUpdate")
    private CmsUpdate cmsUpdate;
}
