package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NdhAssessmentUpdateSoapBody {
    @JsonProperty("CMSUpdate")
    private CmsUpdate cmsUpdate;
}
