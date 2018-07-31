package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class DeliusRiskUpdateResponse {
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

    public Optional<String> getCaseReferenceNumber() {
        return Optional.of(body)
                .map(b -> b.get("RiskUpdateResponse"))
                .map(srdr -> srdr.get("CaseReferenceNumber"))
                .map(JsonNode::textValue);
    }
}
