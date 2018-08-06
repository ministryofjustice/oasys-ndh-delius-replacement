package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliusAssessmentSummaryResponse {
    @JsonProperty("Body")
    private JsonNode body;

    public boolean isBadResponse() {
        return isBad() || isSoapFault();
    }

    public boolean isSoapFault() {
        return body.hasNonNull("Fault");
    }
    public boolean isBad() {
        return body == null;
    }
}
