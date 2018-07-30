package uk.gov.justice.digital.ndh.api.delius.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitRiskDataRequest {
    private Risk risk;
}
