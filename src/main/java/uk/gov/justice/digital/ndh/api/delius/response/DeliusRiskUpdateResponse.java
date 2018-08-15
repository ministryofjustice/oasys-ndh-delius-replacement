package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class DeliusRiskUpdateResponse {
    @JsonProperty("Body")
    private JsonNode body;

    public boolean isSoapFault() {
        return body != null && body.hasNonNull("Fault");
    }

    public Optional<String> getCaseReferenceNumber() {
        return Optional.of(body)
                .map(b -> b.get("SubmitRiskDataResponse"))
                .map(srdr -> srdr.get("CaseReferenceNumber"))
                .map(JsonNode::textValue);
    }
}
